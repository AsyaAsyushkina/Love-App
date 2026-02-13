package com.example.loveapp // Убедитесь, что ваш package совпадает!


import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import kotlinx.coroutines.delay
import kotlin.random.Random

val countHeart = 20
val speed = 2




// remember сохраняет плеер, чтобы он не пересоздавался


@Composable
fun StartScreen(onNavigate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            bitmap = ImageBitmap.imageResource(id = R.drawable.konvert_close),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight()
                .clickable { onNavigate() },
            contentScale = ContentScale.FillWidth,
            filterQuality = FilterQuality.None
        )

    }
}

// 3. Второй экран: Инфо

fun isValidPosition(x: Float, y: Float, listHeart: List<HeartPosition>): Boolean {
    val size = 50f
    for (heart in listHeart) {
        val verticalDifference = Math.abs(heart.y - y)
        val horizontalDifference = Math.abs(heart.x - x)

        if (verticalDifference < size && horizontalDifference < size) {
            return false
        }
    }
    return true
}

@Composable
fun DetailsScreen(addCounter: () -> Unit, finalCount: Int, addHeart: (Float, Float) -> Unit, listHeart: List<HeartPosition>, listFly: MutableList<HeartFly>) {
    val imageHeart = ImageBitmap.imageResource(R.drawable.heart)
    var envelopeImage = ImageBitmap.imageResource(id = R.drawable.konvert_open)

    if(listHeart.size >= countHeart) {
        when(finalCount){
        0 -> envelopeImage = ImageBitmap.imageResource(id = R.drawable.konvert_letter)
        1-> envelopeImage = ImageBitmap.imageResource(id = R.drawable.letter_close)
        2-> envelopeImage = ImageBitmap.imageResource(id = R.drawable.letter_open)
        3-> envelopeImage = ImageBitmap.imageResource(id = R.drawable.letter1)
        4-> envelopeImage = ImageBitmap.imageResource(id = R.drawable.letter2)
        else-> envelopeImage = ImageBitmap.imageResource(id = R.drawable.letter3)

        }

    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ){
        if(finalCount > 4) {
            LaunchedEffect(Unit) {
                while (true) {
                    val angle =
                        Random.nextFloat() * 2 * Math.PI // Случайное направление в 360 градусов
                    val speed = Random.nextFloat() * 4f + 2f     // Случайная скорость

                    listFly += HeartFly(
                        id = Random.nextLong(),
                        x = maxWidth.value / 2,
                        y = maxHeight.value / 2,
                        vx = (Math.cos(angle) * speed).toFloat(),
                        vy = (Math.sin(angle) * speed).toFloat()
                    )
                    delay(500) // Этот delay НЕ вешает экран!
                }
            }

            // АНИМАТОР: Двигает все сердечки каждый кадр
            LaunchedEffect(Unit) {
                while (true) {
                    withFrameMillis {
                        val iterator = listFly.iterator()
                        while (iterator.hasNext()) {
                            val heart = iterator.next()

                            // Обновляем координаты
                            heart.x += heart.vx
                            heart.y += heart.vy


                            // Если вылетело за экран или стало прозрачным — удаляем
                            if (heart.x < -100 || heart.x > maxWidth.value + 100 ||
                                heart.y < -100 || heart.y > maxHeight.value + 100
                            ) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }
        }
        listFly.forEach {
            heart ->
            Image(
                bitmap = imageHeart,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = heart.x.dp, y = heart.y.dp),

                filterQuality = FilterQuality.None
            )
        }

        listHeart.forEach {
            heart ->
            Image(
                bitmap = imageHeart,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = heart.x.dp, y = heart.y.dp),

                filterQuality = FilterQuality.None
            )
        }
        Column(
        modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                bitmap = envelopeImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .wrapContentHeight()
                    .clickable {
                        if(listHeart.size >= countHeart){
                            addCounter()
                        }
                        var rx = 0f
                        var ry = 0f
                        do {
                            rx = Random.nextFloat() * this@BoxWithConstraints.maxWidth.value
                            ry = Random.nextFloat() * this@BoxWithConstraints.maxHeight.value
                        }while(!isValidPosition(rx, ry, listHeart))

                        addHeart(rx, ry)
                               },
                contentScale = ContentScale.FillWidth,
                filterQuality = FilterQuality.None
            )
        }

    }
}


// 1. Главная функция-"дирижер"
@Composable
fun SimpleApp() {
    // Храним состояние: какой экран сейчас показывать
    var currentScreen by remember { mutableStateOf("start") }
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.music).apply {
            isLooping = true // Зацикливаем музыку
            setVolume(0.5f, 0.5f) // Устанавливаем громкость (от 0.0 до 1.0)
        }
    }
    // Храним данные, которые хотим пронести между экранами
    var counter by remember { mutableStateOf(0) }
    val heartPositions = remember { mutableStateListOf<HeartPosition>() }
    var heartFlyPositions = remember { mutableStateListOf<HeartFly>() }

    DisposableEffect(Unit) {
        mediaPlayer.start() // Запуск при входе на экран

        onDispose {
            mediaPlayer.stop() // Остановка при выходе
            mediaPlayer.release() // Освобождение ресурсов (очень важно!)
        }
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                // Настраиваем тип анимации: появление (FadeIn) + масштаб (Scale)
//                (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.8f))
                (fadeIn(animationSpec = tween(500)))
                    .togetherWith(fadeOut(animationSpec = tween(200)))
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                "start" -> StartScreen(
                    onNavigate = { currentScreen = "details" }
                )

                "details" -> DetailsScreen(
                    finalCount = counter,
                    addHeart = {x, y ->
                        if(heartPositions.size < countHeart)
                            heartPositions.add(HeartPosition(Random.nextLong(), x, y))
                               },
                    listHeart = heartPositions,
                    listFly = heartFlyPositions,
                    addCounter = { counter++ }
                )
            }
        }
    }
}




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Оборачиваем в тему, чтобы кнопки и шрифты выглядели правильно
            MaterialTheme {
                // Вызываем вашу главную функцию
                SimpleApp()
            }
        }
    }
}

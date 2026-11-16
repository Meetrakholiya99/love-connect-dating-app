package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R

@Composable
fun ProfilePhoto(
    photoKey: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val drawableRes = when (photoKey) {
        "drawable:sophia" -> R.drawable.img_profile_sophia_1794419112486
        "drawable:marcus" -> R.drawable.img_profile_marcus_1794419111860
        "drawable:elena" -> R.drawable.img_profile_elena_1794647205345
        "drawable:liam" -> R.drawable.img_profile_liam_1794647205809
        "drawable:logo" -> R.drawable.icon_loveconnect_1794419097509
        else -> null
    }

    when {
        drawableRes != null -> {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        !photoKey.isNullOrBlank() -> {
            AsyncImage(
                model = photoKey,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        else -> {
            Box(
                modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

package io.github.rodrigo4sousa.pokedex.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.rodrigo4sousa.pokedex.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull()
    }
    val uriHandler = LocalUriHandler.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.systemBarsPadding()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, end = 16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.about_back)
                    )
                }
                Text(
                    text = stringResource(R.string.about_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name) +
                        (versionName?.let { " " + stringResource(R.string.about_version, it) } ?: ""),
                    style = MaterialTheme.typography.titleMedium
                )

                AboutSection(
                    title = stringResource(R.string.about_disclaimer_title),
                    body = stringResource(R.string.about_disclaimer_body)
                )
                AboutSection(
                    title = stringResource(R.string.about_data_title),
                    body = stringResource(R.string.about_data_body)
                )

                HorizontalDivider()

                Column(modifier = Modifier.fillMaxWidth()) {
                    AboutLink(stringResource(R.string.about_privacy_policy)) {
                        uriHandler.openUri(AboutLinks.PRIVACY_POLICY_URL)
                    }
                    AboutLink(stringResource(R.string.about_terms)) {
                        uriHandler.openUri(AboutLinks.TERMS_URL)
                    }
                    AboutLink(stringResource(R.string.about_source_code)) {
                        uriHandler.openUri(AboutLinks.SOURCE_CODE_URL)
                    }
                    AboutLink(stringResource(R.string.about_pokeapi)) {
                        uriHandler.openUri(AboutLinks.POKEAPI_URL)
                    }
                }

                Text(
                    text = stringResource(R.string.about_license),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AboutLink(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = label)
    }
}

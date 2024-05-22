package com.yangdai.opennote.presentation.component

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.ScreenSearchDesktop
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import com.yangdai.opennote.MainActivity
import com.yangdai.opennote.R
import com.yangdai.opennote.presentation.event.DatabaseEvent
import com.yangdai.opennote.presentation.screen.SettingsItem
import com.yangdai.opennote.presentation.state.AppColor
import com.yangdai.opennote.presentation.state.AppColor.Companion.toInt
import com.yangdai.opennote.presentation.state.AppTheme
import com.yangdai.opennote.presentation.state.AppTheme.Companion.toInt
import com.yangdai.opennote.presentation.theme.DarkBlueColors
import com.yangdai.opennote.presentation.theme.DarkGreenColors
import com.yangdai.opennote.presentation.theme.DarkOrangeColors
import com.yangdai.opennote.presentation.theme.DarkRedColors
import com.yangdai.opennote.presentation.theme.DarkPurpleColors
import com.yangdai.opennote.presentation.util.Constants
import com.yangdai.opennote.presentation.util.rememberCustomTabsIntent
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailPane(
    sharedViewModel: SharedViewModel = hiltViewModel(LocalContext.current as MainActivity),
    selectedSettingsItem: SettingsItem,
    navigateBackToList: () -> Unit
) {

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val settingsState by sharedViewModel.settingsStateFlow.collectAsStateWithLifecycle()
    val folderEntities by sharedViewModel.foldersStateFlow.collectAsStateWithLifecycle()
    val actionState by sharedViewModel.dataActionStateFlow.collectAsStateWithLifecycle()

    val customTabsIntent = rememberCustomTabsIntent()

    val modeOptions = listOf(
        stringResource(R.string.system_default),
        stringResource(R.string.light),
        stringResource(R.string.dark)
    )

    val colorSchemes = listOf(
        Pair(AppColor.PURPLE, DarkPurpleColors),
        Pair(AppColor.BLUE, DarkBlueColors),
        Pair(AppColor.GREEN, DarkGreenColors),
        Pair(AppColor.ORANGE, DarkOrangeColors),
        Pair(AppColor.RED, DarkRedColors)
    )

    val isSystemDarkTheme = isSystemInDarkTheme()

    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showRatingDialog by rememberSaveable { mutableStateOf(false) }

    var folderId: Long? by rememberSaveable { mutableStateOf(null) }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uriList ->
        if (uriList.isNotEmpty()) {
            val contentResolver = context.contentResolver
            sharedViewModel.onDatabaseEvent(
                DatabaseEvent.Import(
                    contentResolver,
                    folderId,
                    uriList
                )
            )
        }
    }

    val recoveryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val contentResolver = context.contentResolver
            sharedViewModel.onDatabaseEvent(DatabaseEvent.Recovery(contentResolver, it))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED)
                TopAppBar(
                    title = {
                        TopBarTitle(title = stringResource(selectedSettingsItem.titleId))
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.largeTopAppBarColors().containerColor)
                )
            else
                LargeTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigateBackToList()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    title = {
                        TopBarTitle(title = stringResource(selectedSettingsItem.titleId))
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors()
                        .copy(scrolledContainerColor = TopAppBarDefaults.largeTopAppBarColors().containerColor)
                )
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            when (selectedSettingsItem.index) {
                0 -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            PaletteImage()
                        }

                        SettingsHeader(text = stringResource(R.string.color))

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clickable {
                                        sharedViewModel.putPreferenceValue(
                                            Constants.Preferences.APP_COLOR,
                                            AppColor.DYNAMIC.toInt()
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    modifier = Modifier.padding(start = 32.dp),
                                    selected = settingsState.color == AppColor.DYNAMIC,
                                    onClick = null
                                )
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 16.dp),
                                    imageVector = Icons.Default.Colorize,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    contentDescription = ""
                                )
                                Text(
                                    text = stringResource(R.string.dynamic_only_android_12),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.Start
                            )
                        ) {
                            Spacer(modifier = Modifier.width(24.dp))
                            colorSchemes.forEach { colorSchemePair ->
                                SelectableColorPlatte(
                                    selected = settingsState.color == colorSchemePair.first,
                                    colorScheme = colorSchemePair.second
                                ) {
                                    sharedViewModel.putPreferenceValue(
                                        Constants.Preferences.APP_COLOR,
                                        colorSchemePair.first.toInt()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(32.dp))
                        }

                        SettingsHeader(text = stringResource(R.string.dark_mode))

                        Column(
                            Modifier.selectableGroup()
                        ) {
                            modeOptions.forEachIndexed { index, text ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (index == settingsState.theme.toInt()),
                                            onClick = {

                                                if (settingsState.theme.toInt() != index) {
                                                    when (index) {
                                                        0 -> {
                                                            if (isSystemDarkTheme != settingsState.isAppInDarkMode) {
                                                                sharedViewModel.putPreferenceValue(
                                                                    Constants.Preferences.IS_SWITCH_ACTIVE,
                                                                    true
                                                                )
                                                            } else {
                                                                sharedViewModel.putPreferenceValue(
                                                                    Constants.Preferences.APP_THEME,
                                                                    AppTheme.SYSTEM.toInt()
                                                                )
                                                            }
                                                            sharedViewModel.putPreferenceValue(
                                                                Constants.Preferences.SHOULD_FOLLOW_SYSTEM,
                                                                true
                                                            )
                                                        }

                                                        1 -> {
                                                            if (settingsState.isAppInDarkMode) {
                                                                sharedViewModel.putPreferenceValue(
                                                                    Constants.Preferences.IS_SWITCH_ACTIVE,
                                                                    true
                                                                )
                                                            }
                                                            sharedViewModel.putPreferenceValue(
                                                                Constants.Preferences.SHOULD_FOLLOW_SYSTEM,
                                                                false
                                                            )
                                                            sharedViewModel.putPreferenceValue(
                                                                Constants.Preferences.APP_THEME,
                                                                AppTheme.LIGHT.toInt()
                                                            )
                                                        }

                                                        2 -> {
                                                            if (!settingsState.isAppInDarkMode) {
                                                                sharedViewModel.putPreferenceValue(
                                                                    Constants.Preferences.IS_SWITCH_ACTIVE,
                                                                    true
                                                                )
                                                            }
                                                            sharedViewModel.putPreferenceValue(
                                                                Constants.Preferences.SHOULD_FOLLOW_SYSTEM,
                                                                false
                                                            )
                                                            sharedViewModel.putPreferenceValue(
                                                                Constants.Preferences.APP_THEME,
                                                                AppTheme.DARK.toInt()
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(start = 32.dp, end = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (index == settingsState.theme.toInt()),
                                        onClick = null
                                    )

                                    Icon(
                                        modifier = Modifier
                                            .padding(start = 16.dp),
                                        imageVector = when (index) {
                                            AppTheme.LIGHT.toInt() -> Icons.Default.LightMode
                                            AppTheme.DARK.toInt() -> Icons.Default.DarkMode
                                            else -> Icons.Default.BrightnessAuto
                                        },
                                        tint = MaterialTheme.colorScheme.secondary,
                                        contentDescription = ""
                                    )

                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .padding(start = 16.dp)

                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        SettingsHeader(text = stringResource(R.string.data))

                        ListItem(
                            modifier = Modifier.clickable {
                                showFolderDialog = true
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "Import"
                                )
                            },
                            headlineContent = { Text(text = stringResource(R.string.import_files)) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.import_files_description)
                                )
                            }
                        )

                        ListItem(
                            modifier = Modifier.clickable {
                                sharedViewModel.onDatabaseEvent(DatabaseEvent.Backup(context.contentResolver))
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Backup,
                                    contentDescription = "Backup"
                                )
                            },
                            headlineContent = { Text(text = stringResource(id = R.string.backup)) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.backup_description)
                                )
                            }
                        )

                        ListItem(
                            modifier = Modifier.clickable {
                                recoveryLauncher.launch(arrayOf("application/json"))
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Restore,
                                    contentDescription = "Restore"
                                )
                            },
                            headlineContent = { Text(text = stringResource(id = R.string.recovery)) },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.restore_description)
                                )
                            }
                        )

                        ListItem(
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.CleaningServices,
                                    contentDescription = "Clear"
                                )
                            },
                            headlineContent = { Text(text = stringResource(R.string.reset_database)) },
                            trailingContent = {
                                TextButton(
                                    onClick = {
                                        showWarningDialog = true
                                    },
                                    colors = ButtonDefaults.textButtonColors().copy(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(text = stringResource(id = R.string.reset))
                                }
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.reset_database_description)
                                )
                            }
                        )

                        SettingsHeader(text = stringResource(R.string.security))

                        ListItem(
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.Fingerprint,
                                    contentDescription = "Password"
                                )
                            },
                            headlineContent = { Text(text = stringResource(R.string.password)) },
                            trailingContent = {
                                Switch(
                                    checked = settingsState.needPassword,
                                    onCheckedChange = { checked ->
                                        val keyguardManager =
                                            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                                        if (keyguardManager.isKeyguardSecure) {
                                            sharedViewModel.putPreferenceValue(
                                                Constants.Preferences.NEED_PASSWORD,
                                                checked
                                            )
                                        } else {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.no_password_set),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.password_description)
                                )
                            }
                        )
                    }
                }

                2 -> {

                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        val packageInfo =
                            context.packageManager.getPackageInfo(
                                context.packageName,
                                0
                            )
                        val version = packageInfo.versionName
                        var pressAMP by remember { mutableFloatStateOf(16f) }
                        val animatedPress by animateFloatAsState(
                            targetValue = pressAMP,
                            animationSpec = tween(), label = ""
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CurlyCornerShape(amp = animatedPress.toDouble()),
                                    )
                                    .shadow(
                                        elevation = 10.dp,
                                        shape = CurlyCornerShape(amp = animatedPress.toDouble()),
                                        ambientColor = MaterialTheme.colorScheme.primaryContainer,
                                        spotColor = MaterialTheme.colorScheme.primaryContainer,
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                pressAMP = 0f
                                                tryAwaitRelease()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                pressAMP = 16f
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    modifier = Modifier.size(180.dp),
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                    contentDescription = "Icon"
                                )
                            }

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.version) + " " + version,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        ListItem(
                            modifier = Modifier.clickable {
                                customTabsIntent.launchUrl(
                                    context,
                                    Uri.parse("https://github.com/YangDai2003/OpenNote-Compose/blob/master/Guide.md")
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.TipsAndUpdates,
                                    contentDescription = "Guide"
                                )
                            },
                            headlineContent = {
                                Text(text = stringResource(R.string.guide))
                            })

                        ListItem(
                            modifier = Modifier.clickable {
                                customTabsIntent.launchUrl(
                                    context,
                                    Uri.parse("https://github.com/YangDai2003/OpenNote-Compose/blob/master/PRIVACY_POLICY.md")
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.PrivacyTip,
                                    contentDescription = "Privacy Policy"
                                )
                            },
                            headlineContent = { Text(text = stringResource(R.string.privacy_policy)) })

                        ListItem(
                            modifier = Modifier.clickable {
                                showRatingDialog = true
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.StarRate,
                                    contentDescription = "Rate"
                                )
                            },
                            headlineContent = { Text(text = stringResource(R.string.rate_this_app)) })

                        ListItem(
                            modifier = Modifier.clickable {
                                val sendIntent = Intent(Intent.ACTION_SEND)
                                sendIntent.setType("text/plain")
                                sendIntent.putExtra(
                                    Intent.EXTRA_TITLE,
                                    context.getString(R.string.app_name)
                                )
                                sendIntent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    context.getString(R.string.shareContent)
                                )
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Outlined.IosShare,
                                    contentDescription = "Share"
                                )
                            },
                            headlineContent = { Text(text = stringResource(R.string.share_this_app)) })
                    }
                }

                else -> {
                    SettingsDetailPlaceHolder()
                }
            }
        }

        if (showRatingDialog) {
            RatingDialog(
                onDismissRequest = { showRatingDialog = false }
            ) { stars ->
                if (stars > 3) {
                    customTabsIntent.launchUrl(
                        context,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.yangdai.opennote")
                    )
                } else {
                    if (stars == 0) return@RatingDialog
                    // 获取当前应用的版本号
                    val packageInfo =
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    val appVersion = packageInfo.versionName
                    val deviceModel = Build.MODEL
                    val systemVersion = Build.VERSION.SDK_INT

                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("dy15800837435@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback - Open Note")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Version: $appVersion\nDevice: $deviceModel\nSystem: $systemVersion\n"
                        )
                    }
                    context.startActivity(
                        Intent.createChooser(
                            emailIntent,
                            "Feedback (E-mail)"
                        )
                    )
                }
            }
        }

        if (showWarningDialog) {
            WarningDialog(
                message = stringResource(R.string.reset_database_warning),
                onDismissRequest = { showWarningDialog = false }
            ) {
                sharedViewModel.onDatabaseEvent(DatabaseEvent.Reset)
            }
        }

        ProgressDialog(
            isLoading = actionState.loading,
            progress = actionState.progress,
            infinite = actionState.infinite,
            errorMessage = actionState.error,
            onDismissRequest = sharedViewModel::cancelDataAction
        )

        if (showFolderDialog) {
            FolderListDialog(
                hint = stringResource(R.string.destination_folder),
                oFolderId = folderId,
                folders = folderEntities,
                onDismissRequest = { showFolderDialog = false }
            ) { id ->
                folderId = id
                importLauncher.launch(arrayOf("text/*"))
            }
        }
    }
}

@Composable
fun SettingsDetailPlaceHolder() =
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {}
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Outlined.ScreenSearchDesktop,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.settings_hint))
    }
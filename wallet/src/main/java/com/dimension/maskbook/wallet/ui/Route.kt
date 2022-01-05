package com.dimension.maskbook.wallet.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.*
import androidx.navigation.compose.dialog
import com.dimension.maskbook.wallet.R
import com.dimension.maskbook.wallet.ext.decodeUrl
import com.dimension.maskbook.wallet.ext.encodeUrl
import com.dimension.maskbook.wallet.ext.observeAsState
import com.dimension.maskbook.wallet.repository.*
import com.dimension.maskbook.wallet.route.backup
import com.dimension.maskbook.wallet.ui.scenes.MainHost
import com.dimension.maskbook.wallet.ui.scenes.app.settings.MarketTrendSettingsModal
import com.dimension.maskbook.wallet.ui.scenes.persona.*
import com.dimension.maskbook.wallet.ui.scenes.persona.social.ConnectSocialModal
import com.dimension.maskbook.wallet.ui.scenes.persona.social.DisconnectSocialDialog
import com.dimension.maskbook.wallet.ui.scenes.register.RegisterScene
import com.dimension.maskbook.wallet.ui.scenes.register.createidentity.CreateIdentityHost
import com.dimension.maskbook.wallet.ui.scenes.register.recovery.IdentityScene
import com.dimension.maskbook.wallet.ui.scenes.register.recovery.PrivateKeyScene
import com.dimension.maskbook.wallet.ui.scenes.register.recovery.RecoveryComplectedScene
import com.dimension.maskbook.wallet.ui.scenes.register.recovery.RecoveryHomeScene
import com.dimension.maskbook.wallet.ui.scenes.register.recovery.local.RecoveryLocalHost
import com.dimension.maskbook.wallet.ui.scenes.register.recovery.remote.remoteBackupRecovery
import com.dimension.maskbook.wallet.ui.scenes.settings.*
import com.dimension.maskbook.wallet.ui.scenes.wallets.WalletQrcodeScene
import com.dimension.maskbook.wallet.ui.scenes.wallets.common.MultiChainWalletDialog
import com.dimension.maskbook.wallet.ui.scenes.wallets.create.CreateOrImportWalletScene
import com.dimension.maskbook.wallet.ui.scenes.wallets.create.CreateType
import com.dimension.maskbook.wallet.ui.scenes.wallets.create.create.CreateWalletHost
import com.dimension.maskbook.wallet.ui.scenes.wallets.create.import.ImportWalletHost
import com.dimension.maskbook.wallet.ui.scenes.wallets.intro.LegalScene
import com.dimension.maskbook.wallet.ui.scenes.wallets.intro.password.FaceIdEnableScene
import com.dimension.maskbook.wallet.ui.scenes.wallets.intro.password.SetUpPaymentPassword
import com.dimension.maskbook.wallet.ui.scenes.wallets.intro.password.TouchIdEnableScene
import com.dimension.maskbook.wallet.ui.scenes.wallets.management.*
import com.dimension.maskbook.wallet.ui.scenes.wallets.send.SendTokenHost
import com.dimension.maskbook.wallet.ui.scenes.wallets.token.TokenDetailScene
import com.dimension.maskbook.wallet.ui.widget.*
import com.dimension.maskbook.wallet.viewmodel.persona.RenamePersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.SwitchPersonaViewModel
import com.dimension.maskbook.wallet.viewmodel.persona.social.DisconnectSocialViewModel
import com.dimension.maskbook.wallet.viewmodel.recovery.IdentityViewModel
import com.dimension.maskbook.wallet.viewmodel.recovery.PrivateKeyViewModel
import com.dimension.maskbook.wallet.viewmodel.register.RemoteBackupRecoveryViewModelBase
import com.dimension.maskbook.wallet.viewmodel.settings.EmailSetupViewModel
import com.dimension.maskbook.wallet.viewmodel.settings.PhoneSetupViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.TokenDetailViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.WalletManagementModalViewModel
import com.dimension.maskbook.wallet.viewmodel.wallets.management.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

val LocalRootNavController =
    staticCompositionLocalOf<NavHostController> { error("No NavHostController") }

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun Route(
    startDestination: String = "Register",
    onBack: () -> Unit,
) {
    val navController = rememberAnimatedNavController()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator
    CompositionLocalProvider(LocalRootNavController provides navController) {
        ModalBottomSheetLayout(
            bottomSheetNavigator,
            sheetBackgroundColor = MaterialTheme.colors.background,
        ) {
            AnimatedNavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { _, _ ->
                    slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween())
                },
                exitTransition = { _, _ ->
                    slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween())
                },
                popEnterTransition = { _, _ ->
                    slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween())
                },
                popExitTransition = { _, _ ->
                    slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween())
                },
            ) {
                navigation(
                    route = "Register",
                    startDestination = "Init",
                ) {
                    composable(
                        "Init",
                    ) {
                        val repository = get<IPersonaRepository>()
                        val persona by repository.currentPersona.observeAsState(initial = null)
                        LaunchedEffect(Unit) {
                            snapshotFlow { persona }
                                .distinctUntilChanged()
                                .collect {
                                    if (it != null) {
                                        navController.navigate("Main") {
                                            popUpTo("Register") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                        }
                        RegisterScene(
                            onCreateIdentity = {
                                navController.navigate("CreateIdentity")
                            },
                            onRecoveryAndSignIn = {
                                navController.navigate("Recovery")
                            },
                            onSynchronization = {

                            },
                        )
                    }
                    composable("CreateIdentity") {
                        CreateIdentityHost(
                            onDone = {
                                navController.navigate("Main") {
                                    popUpTo("Register") {
                                        inclusive = true
                                    }
                                }
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    navigation(startDestination = "Home", route = "Recovery") {
                        composable("Home") {
                            RecoveryHomeScene(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onIdentity = {
                                    navController.navigate("Identity")
                                },
                                onPrivateKey = {
                                    navController.navigate("PrivateKey")
                                },
                                onLocalBackup = {
                                    navController.navigate("LocalBackup")
                                },
                                onRemoteBackup = {
                                    navController.navigate("RemoteBackupRecovery")
                                }
                            )
                        }
                        navigation("RemoteBackupRecovery_Email", "RemoteBackupRecovery") {
                            remoteBackupRecovery(navController)
                        }
                        navigation("LocalBackup_PickFile", "LocalBackup") {
                            composable(
                                "RemoteBackupRecovery_RecoveryLocal/{uri}",
                                arguments = listOf(
                                    navArgument("uri") { type = NavType.StringType },
                                )
                            ) {
                                val uri = it.arguments?.getString("uri")
                                    ?.let { Uri.parse(it) }
                                if (uri != null) {
                                    RecoveryLocalHost(
                                        uri = uri,
                                        onBack = {
                                            navController.popBackStack()
                                        },
                                        onConfirm = {
                                            navController.navigate("Complected") {
                                                popUpTo("Init") {
                                                    inclusive = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            composable("LocalBackup_PickFile") {
                                val filePickerLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.OpenDocument(),
                                    onResult = {
                                        if (it != null) {
                                            navController.navigate(
                                                "RemoteBackupRecovery_RecoveryLocal/${
                                                    it.toString().encodeUrl()
                                                }"
                                            ) {
                                                popUpTo("LocalBackup_PickFile") {
                                                    inclusive = true
                                                }
                                            }
                                        } else {
                                            navController.popBackStack()
                                        }
                                    },
                                )
                                LaunchedEffect(Unit) {
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                }
                            }
                        }
                        composable("Identity") {
                            val viewModel: IdentityViewModel = getViewModel()
                            val identity by viewModel.identity.observeAsState(initial = "")
                            IdentityScene(
                                identity = identity,
                                onIdentityChanged = {
                                    viewModel.setIdentity(it)
                                },
                                onConfirm = {
                                    viewModel.onConfirm()
                                    navController.navigate("Complected") {
                                        popUpTo("Init") {
                                            inclusive = true
                                        }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                },
                            )
                        }
                        composable("PrivateKey") {
                            val viewModel: PrivateKeyViewModel = getViewModel()
                            val privateKey by viewModel.privateKey.observeAsState(initial = "")
                            PrivateKeyScene(
                                privateKey = privateKey,
                                onPrivateKeyChanged = {
                                    viewModel.setPrivateKey(it)
                                },
                                onConfirm = {
                                    viewModel.onConfirm()
                                    navController.navigate("Complected") {
                                        popUpTo("Init") {
                                            inclusive = true
                                        }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                },
                            )
                        }
                        composable("Complected") {
                            RecoveryComplectedScene(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onConfirm = {
                                    navController.navigate("Main") {
                                        popUpTo("Register") {
                                            inclusive = true
                                        }
                                    }
                                },
                            )
                        }

                    }
//                    composable("Welcome") {
//                        val viewModel: WelcomeViewModel = getViewModel()
//                        val persona by viewModel.persona.observeAsState(initial = "")
//                        WelcomeScene(
//                            persona = persona,
//                            onPersonaChanged = {
//                                viewModel.setPersona(it)
//                            },
//                            onNext = {
//                                viewModel.onConfirm()
//                                navController.navigate("Main") {
//                                    popUpTo("Register") {
//                                        inclusive = true
//                                    }
//                                }
//                            },
//                            onBack = {
//                                navController.popBackStack()
//                            }
//                        )
//                    }
                }
                navigation(route = "Main", startDestination = "Home") {
                    composable(
                        "Home",
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "maskwallet://Home"
                            }
                        )
                    ) {
                        MainHost(onBack = onBack)
                    }
                    wallets(navController = navController)
                    settings(navController = navController)
                    composable("ExportPrivateKeyScene") {
                        ExportPrivateKeyScene(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    dialog("Logout") {
                        LogoutDialog(
                            onBack = {
                                navController.popBackStack()
                            },
                            onDone = {
                                navController.popBackStack("Home", inclusive = false)
                            }
                        )
                    }
                    dialog("Delete") {
                        DeleteDialog(
                            onBack = {
                                navController.popBackStack("Home", inclusive = false)
                            }
                        )
                    }
                    bottomSheet("MarketTrendSettings") {
                        MarketTrendSettingsModal()
                    }
                    composable(
                        "PersonaMenu"
                    ) {
                        val persona by get<IPersonaRepository>().currentPersona.observeAsState(
                            initial = null
                        )
                        persona?.let {
                            PersonaMenu(
                                personaData = it,
                                navController = navController,
                                onBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                    bottomSheet("SwitchPersona") {
                        val viewModel = getViewModel<SwitchPersonaViewModel>()
                        val current by viewModel.current.observeAsState(initial = null)
                        val items by viewModel.items.observeAsState(initial = emptyList())

                        current?.let { it1 ->
                            SwitchPersonaModal(
                                currentPersonaData = it1,
                                items = items,
                                onAdd = {
                                    navController.navigate("CreatePersona")
                                },
                                onItemClicked = {
                                    viewModel.switch(it)
                                }
                            )
                        }
                    }
                    bottomSheet("CreatePersona") {
                        CreatePersona(
                            onDone = {
                                navController.popBackStack()
                            }
                        )
                    }
                    bottomSheet(
                        "RenamePersona/{personaId}",
                        arguments = listOf(
                            navArgument("personaId") { type = NavType.StringType },
                        )
                    ) {
                        val personaId = it.arguments?.getString("personaId")?.decodeUrl()
                        if (personaId != null) {
                            val viewModel = getViewModel<RenamePersonaViewModel> {
                                parametersOf(personaId)
                            }
                            val name by viewModel.name.observeAsState(initial = "")
                            RenamePersona(
                                name = name,
                                onNameChanged = {
                                    viewModel.setName(it)
                                },
                                onDone = {
                                    viewModel.confirm()
                                    navController.popBackStack()
                                },
                            )
                        }
                    }
                    bottomSheet(
                        "ConnectSocial/{personaId}/{platform}",
                        arguments = listOf(
                            navArgument("personaId") { type = NavType.StringType },
                            navArgument("platform") { type = NavType.StringType },
                        ),
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "maskwallet://ConnectSocial/{personaId}/{platform}"
                            }
                        )
                    ) {
                        val personaId = it.arguments?.getString("personaId")?.decodeUrl()
                        val platform = it.arguments?.getString("platform")
                            ?.let { PlatformType.valueOf(it) }
//                        val viewModel = when (platform) {
//                            PlatformType.Twitter -> getViewModel<TwitterConnectSocialViewModel>()
//                            PlatformType.Facebook -> getViewModel<FaceBookConnectSocialViewModel>()
//                            else -> null
//                        }
                        if (personaId != null && platform != null) {
                            val repository = get<IPersonaRepository>()
                            ConnectSocialModal(
                                onDone = {
                                    repository.beginConnectingProcess(
                                        personaId = personaId,
                                        platformType = platform,
                                    )
                                    onBack.invoke()
                                }
                            )
                        }
//                        if (viewModel != null && personaId != null && platform != null) {
//                            val items by viewModel.items.observeAsState(initial = emptyList())
//                            if (items.any()) {
//                                ConnectSocialModal(
//                                    onConnect = {
//                                        viewModel.connect(data = it, personaId = personaId)
//                                        navController.popBackStack()
//                                    },
//                                    socials = items
//                                )
//                            } else {
//                                val repository = get<IPersonaRepository>()
//                                ConnectSocialModal(
//                                    onDone = {
//                                        repository.beginConnectingProcess(
//                                            personaId = personaId,
//                                            platformType = platform,
//                                        )
//                                        onBack.invoke()
//                                    }
//                                )
//                            }
//                        }
                    }
                    dialog(
                        "DisconnectSocial/{personaId}/{platform}/{id}",
                        arguments = listOf(
                            navArgument("personaId") { type = NavType.StringType },
                            navArgument("platform") { type = NavType.StringType },
                            navArgument("id") { type = NavType.StringType },
                        )
                    ) {
                        val personaId = it.arguments?.getString("personaId")?.decodeUrl()
                        val platform =
                            it.arguments?.getString("platform")?.let { PlatformType.valueOf(it) }
                        val id = it.arguments?.getString("id")?.decodeUrl()
                        val viewModel = getViewModel<DisconnectSocialViewModel>()
                        if (personaId != null && platform != null && id != null) {
                            DisconnectSocialDialog(
                                onBack = {
                                    navController.popBackStack()
                                },
                                onConfirm = {
                                    when (platform) {
                                        PlatformType.Twitter ->
                                            viewModel.disconnectTwitter(
                                                personaId = personaId,
                                                socialId = id
                                            )
                                        PlatformType.Facebook ->
                                            viewModel.disconnectFacebook(
                                                personaId = personaId,
                                                socialId = id
                                            )
                                    }
                                    navController.popBackStack()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun enableFaceIdOrTouchId(navController: NavController, type: CreateType) {
    val faceId = true // TODO Logic:enable face id or touch id
    navController.navigate(if (faceId) "WalletIntroHostFaceId/$type" else "WalletIntroHostTouchId/$type")
}

@ExperimentalAnimationApi
@ExperimentalMaterialNavigationApi
private fun NavGraphBuilder.wallets(
    navController: NavController
) {
    composable("WalletQrcode") {
        val repository = get<IWalletRepository>()
        val currentWallet by repository.currentWallet.observeAsState(initial = null)
        currentWallet?.let {
            WalletQrcodeScene(
                walletData = it,
                onShare = { /*TODO*/ },
                onBack = { navController.popBackStack() },
                onCopy = {},
            )
        }
    }
    composable(
        "TokenDetail/{id}",
        arguments = listOf(
            navArgument("id") { type = NavType.StringType }
        )
    ) {
        it.arguments?.getString("id")?.let { id ->
            val viewModel = getViewModel<TokenDetailViewModel> {
                parametersOf(id)
            }
            val token by viewModel.tokenData.observeAsState(initial = null)
            val transaction by viewModel.transaction.observeAsState(initial = emptyList())
            val walletTokenData by viewModel.walletTokenData.observeAsState(initial = null)
            walletTokenData?.let { walletTokenData ->
                token?.let { token ->
                    TokenDetailScene(
                        onBack = { navController.popBackStack() },
                        tokenData = token,
                        walletTokenData = walletTokenData,
                        transactions = transaction,
                        onSpeedUp = { },
                        onCancel = { },
                        onSend = {
                            navController.navigate("SendTokenScene/${token.address}")
                        }
                    )
                }
            }
        }
    }
    bottomSheet("SwitchWalletAdd") {
        WalletSwitchAddModal(
            onCreate = {
                navController.navigate("WalletIntroHostLegal/${CreateType.CREATE}")
            },
            onImport = {
                navController.navigate("WalletIntroHostLegal/${CreateType.IMPORT}")
            },
        )
    }
    bottomSheet("SwitchWalletAddWalletConnect") {
        WalletConnectModal()
    }
    bottomSheet("SwitchWallet") {
        val viewModel = getViewModel<WalletSwitchViewModel>()
        val wallet by viewModel.currentWallet.observeAsState(initial = null)
        val wallets by viewModel.wallets.observeAsState(initial = emptyList())
        val chainType by viewModel.network.observeAsState(initial = ChainType.eth)
        wallet?.let { it1 ->
            WalletSwitchScene(
                onBack = { navController.popBackStack() },
                selectedWallet = it1,
                wallets = wallets,
                onWalletSelected = {
                    viewModel.setCurrentWallet(it)
                },
                selectedChainType = chainType,
                onChainTypeSelected = {
                    viewModel.setChainType(it)
                },
                onAddWalletClicked = {
                    navController.navigate("SwitchWalletAdd")
                },
                onWalletConnectClicked = {
                    navController.navigate("SwitchWalletAddWalletConnect")
                },
                onEditMenuClicked = {
                    navController.navigate("WalletSwitchModal/${it.id}")
                }
            )
        }
    }
    bottomSheet(
        "WalletSwitchModal/{id}",
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) {
        it.arguments?.getString("id")?.let { id ->
            val repository = get<IWalletRepository>()
            val wallets by repository.wallets.observeAsState(initial = emptyList())
            wallets.firstOrNull { it.id == id }?.let { wallet ->
                WalletSwitchModal(
                    walletData = wallet,
                    onRename = { navController.navigate("WalletManagementRename/${wallet.id}") },
                    onDelete = {
                        navController.popBackStack()
                        navController.navigate("WalletManagementDeleteDialog/${wallet.id}")
                    },
                    onDisconnect = {
                    }
                )
            }
        }
    }
    bottomSheet("WalletBalancesMenu") {
        val viewModel = getViewModel<WalletManagementModalViewModel>()
        val currentWallet by viewModel.currentWallet.observeAsState(initial = null)
        currentWallet?.let { wallet ->
            WalletManagementModal(
                walletData = wallet,
                onRename = { navController.navigate("WalletManagementRename/${wallet.id}") },
                onBackup = { navController.navigate("WalletManagementBackup") },
                onTransactionHistory = { navController.navigate("WalletManagementTransactionHistory") },
                onDelete = {
                    navController.popBackStack()
                    navController.navigate("WalletManagementDeleteDialog/${wallet.id}")
                },
                onDisconnect = {

                }
            )
        }
    }
    dialog(
        "WalletManagementDeleteDialog/{id}",
        arguments = listOf(
            navArgument("id") { type = NavType.StringType }
        )
    ) {
        it.arguments?.getString("id")?.let { id ->
            val viewModel = getViewModel<WalletDeleteViewModel> {
                parametersOf(id)
            }
            val wallet by viewModel.wallet.observeAsState(initial = null)
            wallet?.let { walletData ->
                val password by viewModel.password.observeAsState(initial = "")
                val canConfirm by viewModel.canConfirm.observeAsState(initial = false)
                WalletDeleteDialog(
                    walletData = walletData,
                    password = password,
                    onPasswordChanged = { viewModel.setPassword(it) },
                    onBack = { navController.popBackStack() },
                    onDelete = {
                        viewModel.confirm()
                        navController.popBackStack()
                    },
                    passwordValid = canConfirm
                )
            }
        }
    }
    composable("WalletManagementBackup") {
        val viewModel = getViewModel<WalletBackupViewModel>()
        val keyStore by viewModel.keyStore.observeAsState(initial = "")
        val privateKey by viewModel.privateKey.observeAsState(initial = "")
        BackupWalletScene(
            keyStore = keyStore,
            privateKey = privateKey,
            onBack = { navController.popBackStack() },
        )
    }
    composable("WalletManagementTransactionHistory") {
        val viewModel = getViewModel<WalletTransactionHistoryViewModel>()
        val transaction by viewModel.transactions.observeAsState(initial = emptyList())
        WalletTransactionHistoryScene(
            onBack = { navController.popBackStack() },
            transactions = transaction,
            onSpeedUp = {
                // TODO:
            },
            onCancel = {
                // TODO:
            }
        )
    }
    bottomSheet(
        "WalletManagementRename/{id}",
        arguments = listOf(
            navArgument("id") { type = NavType.StringType }
        )
    ) {
        it.arguments?.getString("id")?.let { id ->
            val viewModel = getViewModel<WalletRenameViewModel> {
                parametersOf(id)
            }
            val name by viewModel.name.observeAsState(initial = "")
            WalletRenameModal(
                name = name,
                onNameChanged = { viewModel.setName(it) },
                onDone = {
                    viewModel.confirm()
                    navController.popBackStack()
                },
            )
        }
    }
    composable(
        "WalletIntroHostLegal/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        val type = it.arguments?.getString("type")?.let { type ->
            CreateType.valueOf(type)
        } ?: CreateType.CREATE
        val password by get<ISettingsRepository>().paymentPassword.observeAsState(initial = null)
        LegalScene(
            onBack = { navController.popBackStack() },
            onAccept = {
                if (password.isNullOrEmpty()) {
                    navController.navigate("WalletIntroHostPassword/$type")
                } else {
                    enableFaceIdOrTouchId(navController, type)
                }
            },
            onBrowseAgreement = { TODO("Logic:browse service agreement") }
        )
    }

    bottomSheet(
        "WalletIntroHostPassword/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        val type = it.arguments?.getString("type")?.let { type ->
            CreateType.valueOf(type)
        } ?: CreateType.CREATE
        SetUpPaymentPassword(
            onNext = {
                enableFaceIdOrTouchId(navController, type)
            }
        )
    }

    composable(
        "WalletIntroHostFaceId/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        val type = it.arguments?.getString("type")?.let { type ->
            CreateType.valueOf(type)
        } ?: CreateType.CREATE
        FaceIdEnableScene(
            onBack = { navController.popBackStack() },
            onEnable = {
                navController.navigate("WalletIntroHostFaceIdEnableSuccess/$type")
            }
        )
    }

    dialog(
        "WalletIntroHostFaceIdEnableSuccess/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        val type = it.arguments?.getString("type")?.let { type ->
            CreateType.valueOf(type)
        } ?: CreateType.CREATE
        MaskDialog(
            onDismissRequest = {
                navController.navigate("CreateOrImportWallet/${type}")
            },
            title = {
                Text(text = "Activation successful")
            },
            text = {
                Text(text = "Face id has been enabled successfully.")
            },
            icon = {
                Image(
                    painterResource(id = R.drawable.ic_property_1_snccess),
                    contentDescription = null
                )
            },
            buttons = {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("CreateOrImportWallet/${type}")
                    },
                ) {
                    Text(text = "Done")
                }
            }
        )
    }

    composable(
        "WalletIntroHostTouchId/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        val type = it.arguments?.getString("type")?.let { type ->
            CreateType.valueOf(type)
        } ?: CreateType.CREATE
        TouchIdEnableScene(
            onBack = { navController.popBackStack() },
            onEnable = {
                navController.navigate("WalletIntroHostTouchIdEnableSuccess/$type")
            }
        )
    }

    dialog(
        "WalletIntroHostTouchIdEnableSuccess/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        val type = it.arguments?.getString("type")?.let { type ->
            CreateType.valueOf(type)
        } ?: CreateType.CREATE
        MaskDialog(
            onDismissRequest = {
                navController.navigate("CreateOrImportWallet/${type}")
            },
            title = {
                Text(text = "Activation successful")
            },
            text = {
                Text(text = "Touch id has been enabled successfully.")
            },
            icon = {
                Image(
                    painterResource(id = R.drawable.ic_property_1_snccess),
                    contentDescription = null
                )
            },
            buttons = {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate("CreateOrImportWallet/${type}")
                    },
                ) {
                    Text(text = "Done")
                }
            }
        )
    }

    composable(
        "CreateOrImportWallet/{type}",
        arguments = listOf(
            navArgument("type") { type = NavType.StringType },
        )
    ) {
        CreateOrImportWalletScene(
            onBack = { navController.popBackStack() },
            type = it.arguments?.getString("type")?.let { type ->
                CreateType.valueOf(type)
            } ?: CreateType.CREATE
        )
    }

    dialog("MultiChainWalletDialog") {
        MultiChainWalletDialog()
    }

    composable(
        "CreateWallet/{wallet}",
        arguments = listOf(
            navArgument("wallet") { type = NavType.StringType },
        )
    ) {
        it.arguments?.getString("wallet")?.let { wallet ->
            CreateWalletHost(
                wallet = wallet,
                onDone = {
                    navController.navigate(Uri.parse("maskwallet://Home"), navOptions = navOptions {
                        popUpTo("Main") {
                            inclusive = true
                        }
                    })
                },
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(
        "ImportWallet/{wallet}",
        arguments = listOf(
            navArgument("wallet") { type = NavType.StringType },
        )
    ) {
        it.arguments?.getString("wallet")?.let { wallet ->
            ImportWalletHost(
                wallet = wallet,
                onDone = {
                    navController.navigate(Uri.parse("maskwallet://Home"), navOptions = navOptions {
                        popUpTo("Main") {
                            inclusive = true
                        }
                    })
                },
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(
        "SendTokenScene/{token}",
        arguments = listOf(
            navArgument("token") { type = NavType.StringType }
        )
    ) {
        it.arguments?.getString("token")?.let { token ->
            val tokenRepository = get<ITokenRepository>()
            val tokenData by tokenRepository.getTokenByAddress(token).observeAsState(initial = null)
            tokenData?.let { it1 ->
                SendTokenHost(
                    it1,
                    onDone = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialNavigationApi
private fun NavGraphBuilder.settings(
    navController: NavController
) {
    dialog("SetupPasswordDialog") {
        MaskDialog(
            onDismissRequest = {
                navController.popBackStack()
            },
            title = {
                Text(text = " Set up password before backup")
            },
            text = {
                Text(text = "We have detected that you haven’t set up your backup password and/or payment password. Please set up your backup password and/or payment password before you back up.")
            },
            buttons = {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text(text = "OK")
                }
            },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_property_1_note),
                    contentDescription = null
                )
            }
        )
    }
    bottomSheet("LanguageSettings") {
        LanguageSettings(
            onBack = {
                navController.popBackStack()
            }
        )
    }
    bottomSheet("AppearanceSettings") {
        AppearanceSettings(
            onBack = {
                navController.popBackStack()
            }
        )
    }
    bottomSheet("DataSourceSettings") {
        DataSourceSettings(
            onBack = {
                navController.popBackStack()
            }
        )
    }
    bottomSheet("PaymentPasswordSettings") {
        PaymentPasswordSettings(
            onBack = {
                navController.popBackStack()
            },
            onConfirm = {
                navController.navigate("PaymentPasswordSettingsSuccess") {
                    popUpTo("PaymentPasswordSettings") {
                        inclusive = true
                    }
                }
            }
        )
    }
    dialog("PaymentPasswordSettingsSuccess") {
        MaskDialog(
            onDismissRequest = { /*TODO*/ },
            title = {
                Text(text = "Payment Password changed successfully!")
            },
            text = {
                Text(text = "You have successfully changed your payment password.")
            },
            icon = {
                Image(
                    painterResource(id = R.drawable.ic_property_1_snccess),
                    contentDescription = null
                )
            },
            buttons = {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.popBackStack() },
                ) {
                    Text(text = "Done")
                }
            }
        )
    }
    bottomSheet("ChangeBackUpPassword") {
        BackupPasswordSettings(
            onBack = {
                navController.popBackStack()
            },
            onConfirm = {
                navController.navigate("ChangeBackUpPasswordSuccess") {
                    popUpTo("ChangeBackUpPassword") {
                        inclusive = true
                    }
                }
            }
        )
    }
    dialog("ChangeBackUpPasswordSuccess") {
        MaskDialog(
            onDismissRequest = { /*TODO*/ },
            title = {
                Text(text = "Backup Password changed successfully!")
            },
            text = {
                Text(text = "You have successfully changed your backup password.")
            },
            icon = {
                Image(
                    painterResource(id = R.drawable.ic_property_1_snccess),
                    contentDescription = null
                )
            },
            buttons = {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.popBackStack() },
                ) {
                    Text(text = "Done")
                }
            }
        )
    }

    navigation("Settings_ChangeEmail_Setup", "Settings_ChangeEmail") {
        bottomSheet("Settings_ChangeEmail_Setup") {
            val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Code) {
                    navController.navigate("Settings_ChangeEmail_Setup_Code/${it.value.encodeUrl()}")
                }
            }
            val viewModel = getViewModel<EmailSetupViewModel> {
                parametersOf(requestNavigate)
            }
            val value by viewModel.value.observeAsState(initial = "")
            val valid by viewModel.valueValid.observeAsState(initial = true)
            val loading by viewModel.loading.observeAsState(initial = false)
            EmailInputModal(
                email = value,
                onEmailChange = { viewModel.setValue(it) },
                emailValid = valid,
                onConfirm = { viewModel.sendCode(value) },
                buttonEnabled = loading,
                title = "Set Up Email"
            )
        }
        bottomSheet(
            "Settings_ChangeEmail_Setup_Code/{email}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("email")?.let { email ->
                val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                    if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Next) {
                        navController.navigate("Settings_ChangeEmail_Setup_Success") {
                            popUpTo("Settings_ChangeEmail_Setup") {
                                inclusive = true
                            }
                        }
                    }
                }
                val viewModel = getViewModel<EmailSetupViewModel> {
                    parametersOf(requestNavigate)
                }
                val code by viewModel.code.observeAsState(initial = "")
                val valid by viewModel.codeValid.observeAsState(initial = true)
                val loading by viewModel.loading.observeAsState(initial = false)
                val canSend by viewModel.canSend.observeAsState(initial = false)
                val countDown by viewModel.countdown.observeAsState(initial = 60)
                LaunchedEffect(Unit) {
                    viewModel.startCountDown()
                }
                EmailCodeInputModal(
                    email = email,
                    buttonEnabled = loading,
                    title = "Set Up Email",
                    countDown = countDown,
                    canSend = canSend,
                    codeValid = valid,
                    code = code,
                    onCodeChange = { viewModel.setCode(it) },
                    onSendCode = { viewModel.sendCode(email) },
                    onVerify = { viewModel.verifyCode(code, email) }
                )
            }
        }
        dialog("Settings_ChangeEmail_Setup_Success") {
            MaskDialog(
                onDismissRequest = { navController.popBackStack() },
                title = { Text(text = "Email successfully set up!") },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_property_1_snccess),
                        contentDescription = null
                    )
                },
                text = { Text(text = "You have successfully set up your email. ") },
                buttons = {
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.popBackStack() },
                    ) {
                        Text(text = "Done")
                    }
                }
            )
        }
        bottomSheet(
            "Settings_ChangeEmail_Change_Code/{email}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            ),
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("email")?.let { email ->
                val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                    navController.navigate("Settings_ChangeEmail_Change_New")
                }
                val viewModel = getViewModel<EmailSetupViewModel> {
                    parametersOf(requestNavigate)
                }
                val code by viewModel.code.observeAsState(initial = "")
                val valid by viewModel.codeValid.observeAsState(initial = true)
                val loading by viewModel.loading.observeAsState(initial = false)
                val canSend by viewModel.canSend.observeAsState(initial = false)
                val countDown by viewModel.countdown.observeAsState(initial = 60)
                LaunchedEffect(Unit) {
                    viewModel.startCountDown()
                    viewModel.sendCode(email)
                }
                EmailCodeInputModal(
                    email = email,
                    buttonEnabled = loading,
                    title = "Change Email",
                    subTitle = { Text(text = "To change Email, please verify your current Email address") },
                    countDown = countDown,
                    canSend = canSend,
                    codeValid = valid,
                    code = code,
                    onCodeChange = { viewModel.setCode(it) },
                    onSendCode = { viewModel.sendCode(email) },
                    onVerify = { viewModel.verifyCode(code, email) }
                )
            }
        }
        bottomSheet("Settings_ChangeEmail_Change_New") {
            val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Code) {
                    navController.navigate("Settings_ChangeEmail_Change_New_Code/${it.value.encodeUrl()}")
                }
            }
            val viewModel = getViewModel<EmailSetupViewModel> {
                parametersOf(requestNavigate)
            }
            val value by viewModel.value.observeAsState(initial = "")
            val valid by viewModel.valueValid.observeAsState(initial = true)
            val loading by viewModel.loading.observeAsState(initial = false)
            EmailInputModal(
                email = value,
                onEmailChange = { viewModel.setValue(it) },
                emailValid = valid,
                onConfirm = { viewModel.sendCode(value) },
                buttonEnabled = loading,
                title = "Change Email"
            )
        }

        bottomSheet(
            "Settings_ChangeEmail_Change_New_Code/{email}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("email")?.let { email ->
                val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                    if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Next) {
                        navController.navigate("Settings_ChangeEmail_Change_Success") {
                            popUpTo("Settings_ChangeEmail_Change_Code") {
                                inclusive = true
                            }
                        }
                    }
                }
                val viewModel = getViewModel<EmailSetupViewModel> {
                    parametersOf(requestNavigate)
                }
                val code by viewModel.code.observeAsState(initial = "")
                val valid by viewModel.codeValid.observeAsState(initial = true)
                val loading by viewModel.loading.observeAsState(initial = false)
                val canSend by viewModel.canSend.observeAsState(initial = false)
                val countDown by viewModel.countdown.observeAsState(initial = 60)
                LaunchedEffect(Unit) {
                    viewModel.startCountDown()
                }
                EmailCodeInputModal(
                    email = email,
                    buttonEnabled = loading,
                    title = "Change Email",
                    countDown = countDown,
                    canSend = canSend,
                    codeValid = valid,
                    code = code,
                    onCodeChange = { viewModel.setCode(it) },
                    onSendCode = { viewModel.sendCode(email) },
                    onVerify = { viewModel.verifyCode(code, email) }
                )
            }
        }
        dialog("Settings_ChangeEmail_Change_Success") {
            MaskDialog(
                onDismissRequest = { navController.popBackStack() },
                title = { Text(text = "Email successfully changed!") },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_property_1_snccess),
                        contentDescription = null
                    )
                },
                text = { Text(text = "You have successfully changed your email. ") },
                buttons = {
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.popBackStack() },
                    ) {
                        Text(text = "Done")
                    }
                }
            )
        }

    }

    navigation("Settings_ChangePhone_Setup", "Settings_ChangePhone") {
        bottomSheet("Settings_ChangePhone_Setup") {
            val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Code) {
                    navController.navigate("Settings_ChangePhone_Setup_Code/${it.value.encodeUrl()}")
                }
            }
            val viewModel = getViewModel<PhoneSetupViewModel> {
                parametersOf(requestNavigate)
            }
            val regionCode by viewModel.regionCode.observeAsState(initial = "+86")
            val phone by viewModel.value.observeAsState(initial = "")
            val valid by viewModel.valueValid.observeAsState(initial = true)
            val loading by viewModel.loading.observeAsState(initial = true)
            PhoneInputModal(
                regionCode = regionCode,
                onRegionCodeChange = { viewModel.setRegionCode(it) },
                phone = phone,
                onPhoneChange = { viewModel.setValue(it) },
                phoneValid = valid,
                onConfirm = { viewModel.sendCode(regionCode + phone) },
                buttonEnabled = loading,
                title = "Set Up Phone Number",
            )
        }
        bottomSheet(
            "Settings_ChangePhone_Setup_Code/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("phone")?.let { phone ->
                val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                    if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Next) {
                        navController.navigate("Settings_ChangePhone_Setup_Success") {
                            popUpTo("Settings_ChangePhone_Setup") {
                                inclusive = true
                            }
                        }
                    }
                }
                val viewModel = getViewModel<PhoneSetupViewModel> {
                    parametersOf(requestNavigate)
                }
                val code by viewModel.code.observeAsState(initial = "")
                val canSend by viewModel.canSend.observeAsState(initial = false)
                val valid by viewModel.codeValid.observeAsState(initial = true)
                val countDown by viewModel.countdown.observeAsState(initial = 60)
                val loading by viewModel.loading.observeAsState(initial = false)
                PhoneCodeInputModal(
                    phone = phone,
                    code = code,
                    onCodeChange = { viewModel.setCode(it) },
                    canSend = canSend,
                    codeValid = valid,
                    countDown = countDown,
                    buttonEnabled = loading,
                    onSendCode = { viewModel.sendCode(phone) },
                    onVerify = { viewModel.verifyCode(code = code, value = phone) },
                    title = "Set Up Phone Number"
                )
            }
        }
        dialog("Settings_ChangePhone_Setup_Success") {
            MaskDialog(
                onDismissRequest = { navController.popBackStack() },
                title = { Text(text = "Phone number successfully set up!") },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_property_1_snccess),
                        contentDescription = null
                    )
                },
                text = { Text(text = "You have successfully set up your email. ") },
                buttons = {
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.popBackStack() },
                    ) {
                        Text(text = "Done")
                    }
                }
            )
        }


        bottomSheet(
            "Settings_ChangePhone_Change_Code/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("phone")?.let { phone ->
                val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                    if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Next) {
                        navController.navigate("Settings_ChangePhone_Change_New")
                    }
                }
                val viewModel = getViewModel<PhoneSetupViewModel> {
                    parametersOf(requestNavigate)
                }
                val code by viewModel.code.observeAsState(initial = "")
                val canSend by viewModel.canSend.observeAsState(initial = false)
                val valid by viewModel.codeValid.observeAsState(initial = true)
                val countDown by viewModel.countdown.observeAsState(initial = 60)
                val loading by viewModel.loading.observeAsState(initial = false)
                PhoneCodeInputModal(
                    phone = phone,
                    code = code,
                    onCodeChange = { viewModel.setCode(it) },
                    canSend = canSend,
                    codeValid = valid,
                    countDown = countDown,
                    buttonEnabled = loading,
                    onSendCode = { viewModel.sendCode(phone) },
                    onVerify = { viewModel.verifyCode(code = code, value = phone) },
                    title = "Change Phone Number",
                    subTitle = { Text(text = "To change your phone, you need to verify your current phone number.") }
                )
            }
        }
        bottomSheet("Settings_ChangePhone_Change_New") {
            val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Code) {
                    navController.navigate("Settings_ChangePhone_Change_New_Code/${it.value.encodeUrl()}")
                }
            }
            val viewModel = getViewModel<PhoneSetupViewModel> {
                parametersOf(requestNavigate)
            }
            val regionCode by viewModel.regionCode.observeAsState(initial = "+86")
            val phone by viewModel.value.observeAsState(initial = "")
            val valid by viewModel.valueValid.observeAsState(initial = true)
            val loading by viewModel.loading.observeAsState(initial = true)
            PhoneInputModal(
                regionCode = regionCode,
                onRegionCodeChange = { viewModel.setRegionCode(it) },
                phone = phone,
                onPhoneChange = { viewModel.setValue(it) },
                phoneValid = valid,
                onConfirm = { viewModel.sendCode(regionCode + phone) },
                buttonEnabled = loading,
                title = "Change Phone Number",
            )
        }

        bottomSheet(
            "Settings_ChangePhone_Change_New_Code/{phone}",
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("phone")?.let { phone ->
                val requestNavigate: (RemoteBackupRecoveryViewModelBase.NavigateArgs) -> Unit = {
                    if (it.target == RemoteBackupRecoveryViewModelBase.NavigateTarget.Next) {
                        navController.navigate("Settings_ChangePhone_Change_Success") {
                            popUpTo("Settings_ChangePhone_Change_Code") {
                                inclusive = true
                            }
                        }
                    }
                }
                val viewModel = getViewModel<PhoneSetupViewModel> {
                    parametersOf(requestNavigate)
                }
                val code by viewModel.code.observeAsState(initial = "")
                val canSend by viewModel.canSend.observeAsState(initial = false)
                val valid by viewModel.codeValid.observeAsState(initial = true)
                val countDown by viewModel.countdown.observeAsState(initial = 60)
                val loading by viewModel.loading.observeAsState(initial = false)
                PhoneCodeInputModal(
                    phone = phone,
                    code = code,
                    onCodeChange = { viewModel.setCode(it) },
                    canSend = canSend,
                    codeValid = valid,
                    countDown = countDown,
                    buttonEnabled = loading,
                    onSendCode = { viewModel.sendCode(phone) },
                    onVerify = { viewModel.verifyCode(code = code, value = phone) },
                    title = "Change Phone Number"
                )
            }
        }
        dialog("Settings_ChangePhone_Change_Success") {
            MaskDialog(
                onDismissRequest = { navController.popBackStack() },
                title = { Text(text = "Phone number successfully changed!") },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_property_1_snccess),
                        contentDescription = null
                    )
                },
                text = { Text(text = "You have successfully changed your phone number.") },
                buttons = {
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.popBackStack() },
                    ) {
                        Text(text = "Done")
                    }
                }
            )
        }

    }

    backup(navController = navController)
}

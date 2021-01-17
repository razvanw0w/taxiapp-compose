package ro.razvanz.taxiapp

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import ro.razvanz.taxiapp.model.Taxi
import ro.razvanz.taxiapp.network.ConnectionHelper
import ro.razvanz.taxiapp.repository.TaxiDatabase
import ro.razvanz.taxiapp.repository.TaxiRepository
import ro.razvanz.taxiapp.ui.TaxiAppTheme
import ro.razvanz.taxiapp.viewmodel.TaxiViewModel
import ro.razvanz.taxiapp.viewmodel.TaxiViewModelFactory

class MainActivity : AppCompatActivity() {
    val context: Context = this
    val connectionHelper = ConnectionHelper(context)
    val database by lazy { TaxiDatabase.getDatabase(applicationContext) }
    val repository by lazy { TaxiRepository(database.taxiDAO()) }
    val viewModel: TaxiViewModel by viewModels {
        TaxiViewModelFactory(repository, connectionHelper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaxiAppTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: TaxiViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "registration") {
        composable("registration") {
            RegistrationScreen(navController, viewModel)
        }
        composable("management") {
            ManagementScreen(navController, viewModel)
        }
        composable("reports") {
            ReportsScreen(navController, viewModel)
        }
        composable("driver") {
            DriverScreen(navController, viewModel)
        }
    }
}

@Composable
fun CustomScaffold(
    title: String,
    scaffoldState: ScaffoldState,
    navController: NavHostController,
    viewModel: TaxiViewModel,
    body: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.Menu,
                        modifier = Modifier.clickable(onClick = {
                            scaffoldState.drawerState.open()
                        })
                    )
                }
            )
        },
        drawerContent = {
            ListItem(modifier = Modifier.clickable(onClick = {
                navController.navigate("registration")
            })) {
                Text("Registration Screen")
            }
            ListItem(modifier = Modifier.clickable(onClick = {
                navController.navigate("management")
            })) {
                Text("Management Screen")
            }
            ListItem(modifier = Modifier.clickable(onClick = {
                navController.navigate("reports")
            })) {
                Text("Reports Screen")
            }
            ListItem(modifier = Modifier.clickable(onClick = {
                navController.navigate("driver")
            })) {
                Text("Driver Screen")
            }
        },
        scaffoldState = scaffoldState,
        bodyContent = if (viewModel.loading.value) {
            { CircularIndeterminateProgressBar(isDisplayed = viewModel.loading.value) }
        } else body
    )
}

@Composable
fun RegistrationScreen(navController: NavHostController, viewModel: TaxiViewModel) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val loadedState = remember { mutableStateOf(false) }

    CustomScaffold(
        title = "Registration Screen",
        scaffoldState = scaffoldState,
        navController = navController,
        viewModel = viewModel
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val idState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            val nameState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            val statusState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            val sizeState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            val driverState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            val colorState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            val capacityState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            TextField(
                value = idState.value,
                onValueChange = { idState.value = it },
                label = { Text("ID") })
            TextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = { Text("Name") })
            TextField(
                value = statusState.value,
                onValueChange = { statusState.value = it },
                label = { Text("Status") })
            TextField(
                value = sizeState.value,
                onValueChange = { sizeState.value = it },
                label = { Text("Size") })
            TextField(
                value = driverState.value,
                onValueChange = { driverState.value = it },
                label = { Text("Driver") })
            TextField(
                value = colorState.value,
                onValueChange = { colorState.value = it },
                label = { Text("Color") })
            TextField(
                value = capacityState.value,
                onValueChange = { capacityState.value = it },
                label = { Text("Capacity") })
            Button(onClick = {
                val taxi = Taxi(
                    idState.value.text.toInt(),
                    nameState.value.text,
                    statusState.value.text,
                    sizeState.value.text.toInt(),
                    driverState.value.text,
                    colorState.value.text,
                    capacityState.value.text.toInt()
                )
                viewModel.addTaxi(taxi)
            }) {
                Text("Add")
            }
            TaxiList(viewModel, navController, loadedState)
        }
    }
}

@Composable
fun TaxiList(
    viewModel: TaxiViewModel,
    navController: NavHostController,
    loadedState: MutableState<Boolean>
) {
    val taxis = viewModel.taxiList.observeAsState(initial = emptyList()).value
    if (viewModel.connectionHelper.isConnectedToInternet()) {
        if (!loadedState.value) {
            viewModel.syncDataFromServer()
            loadedState.value = true
        }
        LazyColumn(content = {
            this.items(taxis) { taxi ->
                Text("id ${taxi.id} name ${taxi.name} status ${taxi.status} size ${taxi.size} driver ${taxi.driver} color ${taxi.color} capacity ${taxi.capacity}")
            }
        })
    } else {
        Column {
            Text("No connection to internet.")
            Button(onClick = {
                navController.navigate("registration")
            }) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ManagementScreen(navController: NavHostController, viewModel: TaxiViewModel) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val cabToDeleteState = remember { mutableStateOf(0) }
    val loadedState = remember { mutableStateOf(false) }

    CustomScaffold(
        title = "Management Screen",
        scaffoldState = scaffoldState,
        navController = navController,
        viewModel = viewModel
    ) {
        if (viewModel.connectionHelper.isConnectedToInternet()) {
            Column {
                ColorList(viewModel, loadedState)
                Divider(modifier = Modifier.fillMaxWidth())
                CabsForColorList(viewModel, cabToDeleteState, loadedState)
                CabToDeleteButton(viewModel, cabToDeleteState, loadedState)
                CircularIndeterminateProgressBar(isDisplayed = viewModel.loading.value)
            }
        } else {
            Text("No connection to internet.")
        }
    }
}

@Composable
fun ColorList(viewModel: TaxiViewModel, loadedState: MutableState<Boolean>) {
    val list = viewModel._colorList.observeAsState(emptyList()).value
    if (!loadedState.value) {
        viewModel.syncColorsFromServer()
        loadedState.value = true
    }
    LazyColumn(content = {
        this.items(list) { color ->
            Text(
                text = color,
                modifier = Modifier.clickable(onClick = { viewModel.syncCabsByColorFromServer(color) })
            )
        }
    })
}

@Composable
fun CabsForColorList(
    viewModel: TaxiViewModel,
    cabToDeleteState: MutableState<Int>,
    loadedState: MutableState<Boolean>
) {
    val list = viewModel._cabsForColorList.observeAsState(emptyList()).value
    LazyColumn(content = {
        this.items(list) { taxi ->
            Text(
                "id ${taxi.id} name ${taxi.name} status ${taxi.status} size ${taxi.size} driver ${taxi.driver} color ${taxi.color} capacity ${taxi.capacity}",
                modifier = Modifier.clickable(onClick = { cabToDeleteState.value = taxi.id })
            )
        }
    })
}

@Composable
fun CabToDeleteButton(
    viewModel: TaxiViewModel,
    cabToDeleteState: MutableState<Int>,
    loadedState: MutableState<Boolean>
) {
    if (cabToDeleteState.value != 0) {
        Button(onClick = {
            viewModel.deleteTaxi(cabToDeleteState.value)
        }) {
            Text("Delete ${cabToDeleteState.value}")
        }
    }
}

@Composable
fun ReportsScreen(navController: NavHostController, viewModel: TaxiViewModel) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val loadedState = remember { mutableStateOf(false) }

    CustomScaffold(
        title = "Reports Screen",
        scaffoldState = scaffoldState,
        navController = navController,
        viewModel = viewModel
    ) {
        if (viewModel.connectionHelper.isConnectedToInternet()) {
            if (!loadedState.value) {
                viewModel.syncTop10CabsBySize()
                viewModel.syncTop5BiggestCabs()
                viewModel.syncTop10Drivers()
                loadedState.value = true
            }

            Column {
                ReportSizeList(viewModel)
                Divider(modifier = Modifier.fillMaxWidth())
                ReportDriverList(viewModel)
                Divider(modifier = Modifier.fillMaxWidth())
                ReportCapacityList(viewModel)
                CircularIndeterminateProgressBar(isDisplayed = viewModel.loading.value)
            }
        } else {
            Text("No connection to internet.")
        }
    }
}

@Composable
fun ReportSizeList(viewModel: TaxiViewModel) {
    val list = viewModel._reportSize.observeAsState(emptyList()).value
    LazyColumn(content = {
        this.items(list) { taxi ->
            Text(
                "${taxi.name} ${taxi.status} ${taxi.size} ${taxi.driver}"
            )
        }
    })
}

@Composable
fun ReportDriverList(viewModel: TaxiViewModel) {
    val list = viewModel._reportNumberOfCabs.observeAsState(emptyList()).value
    LazyColumn(content = {
        this.items(list) {
            Text(
                "${it.name} ${it.cabs}"
            )
        }
    })
}

@Composable
fun ReportCapacityList(viewModel: TaxiViewModel) {
    val list = viewModel._reportCapacity.observeAsState(emptyList()).value
    LazyColumn(content = {
        this.items(list) { taxi ->
            Text(
                "${taxi.name} ${taxi.capacity}"
            )
        }
    })
}

@Composable
fun DriverScreen(navController: NavHostController, viewModel: TaxiViewModel) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val showDialogState = remember { mutableStateOf(false) }
    val selectedTaxiState = remember { mutableStateOf(Taxi()) }
    val loadedState = remember { mutableStateOf(false) }

    CustomScaffold(
        title = "Driver Screen",
        scaffoldState = scaffoldState,
        navController = navController,
        viewModel = viewModel
    ) {
        Column {
            val driverState = remember {
                mutableStateOf(TextFieldValue(""))
            }
            TextField(
                value = driverState.value,
                onValueChange = { driverState.value = it },
                label = { Text("Driver name") })
            ChangeDriverButton(driverState, viewModel)
            CabListByDriver(viewModel, showDialogState, selectedTaxiState, loadedState)
            TaxiInfoDialog(showDialogState, selectedTaxiState)
            CircularIndeterminateProgressBar(isDisplayed = viewModel.loading.value)
        }
    }
}

@Composable
fun CabListByDriver(
    viewModel: TaxiViewModel,
    showDialogState: MutableState<Boolean>,
    selectedTaxiState: MutableState<Taxi>,
    loadedState: MutableState<Boolean>
) {
    val sharedPreferences = viewModel.connectionHelper.context.getSharedPreferences("driver", 0)
    val driver = sharedPreferences.getString("driver", "") ?: ""
    if (!loadedState.value) {
        viewModel.syncCabsForDriver(driver)
        loadedState.value = true
    }
    val list = viewModel._cabsForDriver.observeAsState(emptyList()).value
    LazyColumn(content = {
        this.items(list) { taxi ->
            Text(
                "name ${taxi.name} status ${taxi.status} size ${taxi.size}",
                modifier = Modifier.clickable(onClick = {
                    selectedTaxiState.value = taxi
                    showDialogState.value = true
                })
            )
        }
    })
}

@Composable
fun TaxiInfoDialog(showDialogState: MutableState<Boolean>, selectedTaxiState: MutableState<Taxi>) {
    val taxi = selectedTaxiState.value
    if (showDialogState.value) {
        AlertDialog(
            onDismissRequest = {
                showDialogState.value = false
            },
            title = {
                Text("Details for Taxi ${taxi.id}")
            },
            text = {
                Text("id ${taxi.id} name ${taxi.name} status ${taxi.status} size ${taxi.size} driver ${taxi.driver} color ${taxi.color} capacity ${taxi.capacity}")
            },
            confirmButton = {
                Button(onClick = {
                    showDialogState.value = false
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ChangeDriverButton(driverState: MutableState<TextFieldValue>, viewModel: TaxiViewModel) {
    Button(onClick = {
        val context = viewModel.connectionHelper.context
        val driverPreference = context.getSharedPreferences("driver", 0).edit()
        driverPreference.putString("driver", driverState.value.text)
        driverPreference.commit()
        viewModel.syncCabsForDriver(driverState.value.text)
    }) {
        Text("Change driver")
    }
}

@Composable
fun CircularIndeterminateProgressBar(isDisplayed: Boolean) {
    if (isDisplayed) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary
            )
        }
    }
}
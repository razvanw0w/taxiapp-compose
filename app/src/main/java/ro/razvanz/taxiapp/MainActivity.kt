package ro.razvanz.taxiapp

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.navigation.compose.*
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.razvanz.taxiapp.model.Taxi
import ro.razvanz.taxiapp.network.ConnectionHelper
import ro.razvanz.taxiapp.remote.TaxiRemoteApi
import ro.razvanz.taxiapp.repository.TaxiDatabase
import ro.razvanz.taxiapp.repository.TaxiRepository
import ro.razvanz.taxiapp.ui.TaxiAppTheme
import ro.razvanz.taxiapp.viewmodel.TaxiViewModel
import ro.razvanz.taxiapp.viewmodel.TaxiViewModelFactory
import java.sql.Driver

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
            ManagementScreen(navController)
        }
        composable("reports") {
            ReportsScreen(navController)
        }
        composable("driver") {
            DriverScreen(navController)
        }
    }
}

@Composable
fun CustomScaffold(
    title: String,
    scaffoldState: ScaffoldState,
    navController: NavHostController,
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
        bodyContent = body
    )
}

@Composable
fun RegistrationScreen(navController: NavHostController, viewModel: TaxiViewModel) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val loadedState = remember { mutableStateOf(false) }

    CustomScaffold(
        title = "Registration Screen",
        scaffoldState = scaffoldState,
        navController = navController
    ) {
        Column (modifier = Modifier.fillMaxWidth()) {
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
            TextField(value = idState.value, onValueChange = {idState.value = it}, label = { Text("ID")})
            TextField(value = nameState.value, onValueChange = {nameState.value = it}, label = { Text("Name")})
            TextField(value = statusState.value, onValueChange = {statusState.value = it}, label = { Text("Status")})
            TextField(value = sizeState.value, onValueChange = {sizeState.value = it}, label = { Text("Size")})
            TextField(value = driverState.value, onValueChange = {driverState.value = it}, label = { Text("Driver")})
            TextField(value = colorState.value, onValueChange = {colorState.value = it}, label = { Text("Color")})
            TextField(value = capacityState.value, onValueChange = {capacityState.value = it}, label = { Text("Capacity")})
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
fun TaxiList(viewModel: TaxiViewModel, navController: NavHostController, loadedState: MutableState<Boolean>) {
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
    }
    else {
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
fun ManagementScreen(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    CustomScaffold(
        title = "Management Screen",
        scaffoldState = scaffoldState,
        navController = navController
    ) {
        Text("Management!")
    }
}

@Composable
fun ReportsScreen(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    CustomScaffold(
        title = "Reports Screen",
        scaffoldState = scaffoldState,
        navController = navController
    ) {
        Text("Reports!")
    }
}

@Composable
fun DriverScreen(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    CustomScaffold(
        title = "Driver Screen",
        scaffoldState = scaffoldState,
        navController = navController
    ) {
        Text("Driver!")
    }
}



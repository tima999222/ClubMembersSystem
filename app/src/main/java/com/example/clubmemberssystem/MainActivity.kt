package com.example.clubmemberssystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clubmemberssystem.ui.theme.ClubMembersSystemTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClubMember(
    val id: Int,
    var surname: String,
    var name: String,
    var patronymic: String,
    var bicycleType: String,
    var experience: Double
)

class ClubMemberModel {
    private val list = mutableListOf<ClubMember>()

    init {
        list.add(ClubMember(1, "aaa", "aaa", "aaa", "FEEW", 2.0))
        list.add(ClubMember(2, "bbb", "bbb", "bbb", "FEFG", 0.5))
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    private val mutableFlow = MutableStateFlow(list.toList())
    val flow = mutableFlow.asStateFlow()

    private fun notifyFlow() {
        scope.launch {
            mutableFlow.emit(list.toList())
        }
    }

    fun add(surname: String,
            name: String,
            patronymic: String,
            bicycleType: String,
            exp: Double) {
        val id = list.count() + 1

        val clubMember = ClubMember(id, surname, name, patronymic, bicycleType, exp)

        list.add(clubMember)
        notifyFlow()
    }

    fun update(id: Int, clubMember: ClubMember) {
        list.forEachIndexed { index, item ->
            if (item.id == id) {
                list[index] = clubMember
            }
        }
        notifyFlow()
    }

    fun delete(clubMember: ClubMember) {
        list.find { it == clubMember }?.let { list.remove(it) }
        notifyFlow()
    }

    fun sortByExperience() {
        list.sortBy { it.experience }
        notifyFlow()
    }

    fun sortBySurname() {
        list.sortBy { it.surname }
        notifyFlow()
    }

    fun findBySurname(surname: String) {
        list.filter { it.surname.contains(surname) }
        notifyFlow()
    }
}

class ListViewModel: ViewModel() {
    private val model = ClubMemberModel()

    val flow = model.flow

    fun add(surname: String,
            name: String,
            patronymic: String,
            bicycleType: String,
            exp: Double) = model.add(surname, name, patronymic, bicycleType, exp)

    fun delete(clubMember: ClubMember) = model.delete(clubMember)

    fun update(id: Int, clubMember: ClubMember) = model.update(id, clubMember)

    fun sortByExp() = model.sortByExperience()

    fun sortBySurname() = model.sortBySurname()

    fun findBySurname(surname: String) = model.findBySurname(surname)
}

class MainActivity : ComponentActivity() {
    private val viewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClubMembersSystemTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    List(viewModel)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowList(listViewModel: ListViewModel, navController: NavHostController) {
    val listState = listViewModel.flow.collectAsState()

    var surname by rememberSaveable {
        mutableStateOf("")
    }

    Column {
        Row {
            Button(onClick = { navController.navigate("add") }) {
                Text(stringResource(R.string.AddClubMember))
            }

            Button(onClick = { listViewModel.sortBySurname() }) {
                Text(stringResource(R.string.surname_sort))
            }

            Button(onClick = { listViewModel.sortByExp() }) {
                Text(stringResource(R.string.exp_sort))
            }

            TextField(
                value = surname,
                onValueChange = {
                    listViewModel.findBySurname(it)
                },
                label = { Text(stringResource(R.string.surname)) }
            )
        }


        LazyColumn {
            items(listState.value,
                key = { it.id }
            ) {
                Row {
                    Text(text = "${it.surname} ${it.name} ${it.patronymic}|${it.bicycleType}|${it.experience} лет опыта", modifier = Modifier.widthIn(max=300.dp))
                    IconButton(onClick = { navController.navigate("edit/${it.id}") }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.edit))
                    }
                    IconButton(onClick = { listViewModel.delete(it) }) {
                        Icon(Icons.Default.Delete, stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToList(viewModel: ListViewModel, navController: NavHostController) {
    Column {
        var surname by rememberSaveable { mutableStateOf("") }
        var name by rememberSaveable { mutableStateOf("") }
        var patronymic by rememberSaveable { mutableStateOf("") }
        var bicycleType by rememberSaveable { mutableStateOf("") }
        var exp by rememberSaveable { mutableStateOf("") }

        TextField(
            value = surname,
            onValueChange = {surname = it},
            label = { Text(stringResource(R.string.surname)) }
        )

        TextField(
            value = name,
            onValueChange = {name = it},
            label = { Text(stringResource(R.string.name)) }
        )

        TextField(
            value = patronymic,
            onValueChange = {patronymic = it},
            label = { Text(stringResource(R.string.patronymic)) }
        )

        TextField(
            value = bicycleType,
            onValueChange = {bicycleType = it},
            label = { Text(stringResource(R.string.bicycle_type)) }
        )

        TextField (
            value = exp,
            onValueChange = {exp = it},
            label = { Text(stringResource(R.string.experience)) }
        )

        Button(
            onClick = {
                viewModel.add(surname, name, patronymic, bicycleType, exp.toDouble())
                navController.popBackStack()
            }
        ) {
            Text(stringResource(R.string.add))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Edit(id: Int, viewModel: ListViewModel, navController: NavHostController) {
    val list = viewModel.flow.collectAsState()

    val clubMember = list.value.first { it.id == id }

    Column {
        var surname by rememberSaveable {
            mutableStateOf(clubMember.surname)
        }
        var name by rememberSaveable {
            mutableStateOf(clubMember.name)
        }
        var patronymic by rememberSaveable {
            mutableStateOf(clubMember.patronymic)
        }
        var bicycleType by rememberSaveable {
            mutableStateOf(clubMember.bicycleType)
        }
        var exp by rememberSaveable {
            mutableStateOf(clubMember.experience.toString())
        }

        TextField(
            value = surname,
            onValueChange = {surname = it},
            label = { Text(stringResource(R.string.surname)) }
        )

        TextField(
            value = name,
            onValueChange = {name = it},
            label = { Text(stringResource(R.string.name)) }
        )

        TextField(
            value = patronymic,
            onValueChange = {patronymic = it},
            label = { Text(stringResource(R.string.patronymic)) }
        )

        TextField(
            value = bicycleType,
            onValueChange = {bicycleType = it},
            label = { Text(stringResource(R.string.bicycle_type)) }
        )

        TextField (
            value = exp,
            onValueChange = {exp = it},
            label = { Text(stringResource(R.string.experience)) }
        )

        Button(
            onClick = {
                viewModel.update(id, clubMember)//add(surname, name, patronymic, bicycleType, exp.toDouble())
                navController.popBackStack()
            }
        ) {
            Text(stringResource(R.string.edit))
        }
    }
}

@Composable
private fun List(viewModel: ListViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ShowList(listViewModel = viewModel, navController = navController)
        }

        composable("add") {
            AddToList(viewModel, navController)
        }

        composable("edit/$id",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                }
            )
        ) {
            Edit(it.arguments!!.getInt("id"), viewModel, navController)
        }
    }
}



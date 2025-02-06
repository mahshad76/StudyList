package com.raywenderlich.listmaker.ui.main

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.raywenderlich.listmaker.models.TaskList

class MainViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {
  //if the tablet is the device, the you need only one view model to interact from the main activity to two other fragments.
  lateinit var onListAdded: (() -> Unit)

  lateinit var tasksList: TaskList

  lateinit var onTaskAdded: (() -> Unit)

  val lists: MutableList<TaskList> by lazy {
    retrieveLists()
  }

  private fun retrieveLists(): MutableList<TaskList> {

    val sharedPreferencesContents = sharedPreferences.all
    val taskLists = ArrayList<TaskList>()

    for (taskList in sharedPreferencesContents) {
      val itemsHashSet = ArrayList(taskList.value as HashSet<String>)
      val list = TaskList(taskList.key, itemsHashSet)
      taskLists.add(list)
    }

    return taskLists
  }

  fun saveList(list: TaskList) {
    sharedPreferences.edit().putStringSet(list.name, list.tasks.toHashSet()).apply()
    lists.add(list)
    onListAdded.invoke()
  }

  fun updateList(list: TaskList) {
    sharedPreferences.edit().putStringSet(list.name, list.tasks.toHashSet()).apply()
    lists.add(list)
  }

  fun refreshLists() {
    lists.clear()
    lists.addAll(retrieveLists())
  }

  ///Managing the list detail view model with main view model when tablet is used.

  lateinit var list: TaskList
  fun addTask(task: String) {
    list.tasks.add(task)
    onTaskAdded.invoke()
  }
}
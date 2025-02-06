package com.raywenderlich.listmaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.raywenderlich.listmaker.databinding.MainActivityBinding
import com.raywenderlich.listmaker.models.TaskList
import com.raywenderlich.listmaker.ui.detail.ListDetailActivity
import com.raywenderlich.listmaker.ui.detail.ui.detail.ListDetailFragment
import com.raywenderlich.listmaker.ui.main.MainFragment
import com.raywenderlich.listmaker.ui.main.MainViewModel
import com.raywenderlich.listmaker.ui.main.MainViewModelFactory

class MainActivity : AppCompatActivity(), MainFragment.MainFragmentInteractionListener {

    private lateinit var binding: MainActivityBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(PreferenceManager.getDefaultSharedPreferences(this))
        )
            .get(MainViewModel::class.java)

        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Log.i("MainActivity", viewModel.toString())
        //Connecting the fragment to the activity. Check the layout which is used by the activity to see if it has fragment container view. Otherwise, the id of the fragment layout is taken.
        if (savedInstanceState == null) {
            val fragmentContainerViewId: Int =
                if (binding.mainFragmentContainer == null) {
                    R.id.main
                } else {
                    R.id.main_fragment_container
                }
            val mainFragment = MainFragment.newInstance()
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                //adding main fragment to the fragment container which is fragment container view or fragment layout.
                add(fragmentContainerViewId, mainFragment)
            }
        }

        binding.fabButton.setOnClickListener {
            //based on the layout which main activity interacts with, if it is for tablet, the button has to manage two responsibilities.
            //showCreateTaskDialog()
            showCreateListDialog()
        }
    }

    private fun showCreateListDialog() {

        val dialogTitle = getString(R.string.name_of_list)
        val positiveButtonTitle = getString(R.string.create_list)

        val builder = AlertDialog.Builder(this)
        val listTitleEditText = EditText(this)
        listTitleEditText.inputType = InputType.TYPE_CLASS_TEXT

        builder.setTitle(dialogTitle)
        builder.setView(listTitleEditText)
        builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
            dialog.dismiss()

            val taskList = TaskList(listTitleEditText.text.toString())
            viewModel.saveList(taskList)
            showListDetail(taskList)
        }

        builder.create().show()
    }

    private fun showCreateTaskDialog() {
        //1
        val taskEditText = EditText(this)
        taskEditText.inputType = InputType.TYPE_CLASS_TEXT

        //2
        AlertDialog.Builder(this)
            .setTitle(R.string.task_to_add)
            .setView(taskEditText)
            .setPositiveButton(R.string.add_task) { dialog, _ ->
                // 3
                val task = taskEditText.text.toString()
                // 4
                viewModel.addTask(task)
                //5
                dialog.dismiss()
            }
            //6
            .create()
            .show()
    }

    private fun showListDetail(list: TaskList) {
        if (binding.listDetailFragmentContainer == null) {
            val listDetailIntent = Intent(this, ListDetailActivity::class.java)
            //in the conversation between the activities, INTENT_LIST_KEY and LIST_DETAIL_REQUEST_CODE show that the request is coming from each activity and the response coming from which one.
            listDetailIntent.putExtra(INTENT_LIST_KEY, list)
            //waiting until hearing back from the list detail activity.
            startActivityForResult(listDetailIntent, LIST_DETAIL_REQUEST_CODE)
        } else {
            val bundle = bundleOf(INTENT_LIST_KEY to list)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(
                    R.id.list_detail_fragment_container,
                    ListDetailFragment::class.java, bundle, null
                )
            }
            binding.fabButton.setOnClickListener { showCreateTaskDialog() }

        }
    }

    override fun listItemTapped(list: TaskList) {
        showListDetail(list)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data:
        Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // 1
        if (requestCode == LIST_DETAIL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 2
            data?.let {
                // 3
                viewModel.updateList(data.getParcelableExtra(INTENT_LIST_KEY)!!)
                viewModel.refreshLists()
            }
        }
    }

    companion object {
        const val INTENT_LIST_KEY = "list"
        const val LIST_DETAIL_REQUEST_CODE = 123
    }

    override fun onBackPressed() {
        val listDetailFragment =
            supportFragmentManager.findFragmentById(R.id.list_detail_fragment_container)
        if (listDetailFragment == null) {
            super.onBackPressed()

        } else {
            title = resources.getString(R.string.app_name)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                remove(listDetailFragment)
            }
            binding.fabButton.setOnClickListener { showCreateListDialog() }
        }
    }
}
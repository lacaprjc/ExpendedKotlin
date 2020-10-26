package com.lacaprjc.expended.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentSettingsBinding
import com.lacaprjc.expended.util.DataFormat
import com.lacaprjc.expended.util.DataState
import com.lacaprjc.expended.util.fileExtension
import com.lacaprjc.expended.util.mimeTypes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.FileNotFoundException
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    companion object {
        const val PICK_FILE_REQUEST_CODE = 100
        const val CREATE_DOCUMENT_REQUEST_CODE = 200
    }

    private lateinit var binding: FragmentSettingsBinding

    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSettingsBinding.bind(view)

        binding.settingsToolbar.pageTitle.text = getString(R.string.settings_toolbar_text)

        binding.deleteAllButton.setOnClickListener {
            MaterialAlertDialogBuilder(it.context)
                .setTitle("Delete Everything")
                .setMessage("Are you sure you want to delete everything? There is no going back from this operation so please make sure to backup your accounts beforehand.")
                .setPositiveButton("Delete") { dialog, _ ->
                    settingsViewModel.deleteAll()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }

        binding.exportButton.setOnClickListener {
            MaterialAlertDialogBuilder(it.context)
                .setTitle("Export Format")
                .setItems(arrayOf("JSON", "CSV")) { dialog, format ->
                    val dataFormat = DataFormat.values()[format]
                    settingsViewModel.setCurrentDataFormat(dataFormat)

                    chooseExportFileLocation(dataFormat)
                    dialog.dismiss()
                }
                .show()
        }

        binding.importButton.setOnClickListener {
            MaterialAlertDialogBuilder(it.context)
                .setTitle("Import Format")
                .setItems(
                    arrayOf(
                        "JSON",
                        "CSV",
                        "Sembast (Deprecated. You may lose your transactions' pictures with this method)"
                    )
                ) { dialog, format ->
                    val dataFormat = DataFormat.values()[format]
                    settingsViewModel.setCurrentDataFormat(dataFormat)

                    chooseImportFile(dataFormat)
                    dialog.dismiss()
                }
                .show()
        }

        subscribeObservers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        val mode = if (requestCode == PICK_FILE_REQUEST_CODE) "r" else "w"

        dataIntent?.data?.let { dataUri ->
            with(requireActivity()) {
                val file = try {
                    contentResolver.openFileDescriptor(
                        dataUri,
                        mode
                    )!!

                } catch (e: FileNotFoundException) {
                    Toast.makeText(
                        this,
                        "Cannot open file ${dataUri.path}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }

                when (requestCode) {
                    PICK_FILE_REQUEST_CODE -> {
                        settingsViewModel.import(file.fileDescriptor)
                    }
                    CREATE_DOCUMENT_REQUEST_CODE -> {
                        settingsViewModel.export(file.fileDescriptor, requireContext())
                    }
                    else -> {
                        return
                    }
                }
            }
        }
    }

    private fun subscribeObservers() {
        settingsViewModel.getDataState().observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.progressBar.hide()
                    when (it.data) {
                        is String -> {
                            Toast.makeText(
                                requireContext(),
                                it.data,
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().navigateUp()
                        }
                        else -> {
                        }
                    }

                    settingsViewModel.setDataState(DataState.Idle)
                }
                is DataState.Failed -> {
                    binding.progressBar.hide()
                    Toast.makeText(
                        requireContext(),
                        it.exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                    settingsViewModel.setDataState(DataState.Idle)

                    it.exception.printStackTrace()
                }
                DataState.Loading -> {
                    binding.progressBar.show()
                }
                DataState.Idle -> {
                    binding.progressBar.hide()
                }
            }
        }
    }

    private fun chooseExportFileLocation(dataFormat: DataFormat) {
        with(requireContext()) {
            getDatabasePath(getString(R.string.database_name))?.absoluteFile.let { file ->
                val dataIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = dataFormat.mimeTypes.first()

                    putExtra(Intent.EXTRA_MIME_TYPES, dataFormat.mimeTypes.toTypedArray())

                    putExtra(
                        Intent.EXTRA_TITLE,
                        "Expended_backup_${
                            LocalDateTime.now()
                        }${dataFormat.fileExtension}"
                    )

                    addCategory(Intent.CATEGORY_OPENABLE)
                }

                startActivityForResult(
                    Intent.createChooser(dataIntent, null),
                    CREATE_DOCUMENT_REQUEST_CODE
                )
            }
        }
    }

    private fun chooseImportFile(dataFormat: DataFormat) {
        val requestFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = dataFormat.mimeTypes.first()

            if (dataFormat != DataFormat.SEMBAST) {
                putExtra(Intent.EXTRA_MIME_TYPES, dataFormat.mimeTypes.toTypedArray())
            }
        }

        val shareIntent: Intent = Intent.createChooser(requestFileIntent, null)
        startActivityForResult(shareIntent, PICK_FILE_REQUEST_CODE)
    }
}


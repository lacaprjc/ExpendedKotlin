package com.lacaprjc.expended.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lacaprjc.expended.R
import com.lacaprjc.expended.databinding.FragmentSettingsBinding
import com.lacaprjc.expended.util.DataFormat
import com.lacaprjc.expended.util.DataState
import com.lacaprjc.expended.util.fileExtension
import com.lacaprjc.expended.util.mimeType
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
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)

        binding = FragmentSettingsBinding.bind(view)

        binding.settingsToolbar.pageTitle.text = getString(R.string.settings_toolbar_text)

        binding.exportButton.setOnClickListener {
            MaterialAlertDialogBuilder(it.context)
                .setTitle("Export Format")
                .setItems(arrayOf("JSON", "CSV", "SQL")) { dialog, format ->
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
                        "SQL",
                        "Sembast (from version ~1.0.3. You may lose your transactions' pictures with this method)"
                    )
                ) { dialog, format ->
                    val dataFormat = DataFormat.values()[format]

                    val mimeType = when (dataFormat) {
                        DataFormat.JSON -> TODO()
                        DataFormat.CSV -> TODO()
                        DataFormat.SQL -> TODO()
                        DataFormat.SEMBAST -> "*/*"
                    }

                    settingsViewModel.setCurrentDataFormat(dataFormat)

                    val requestFileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = mimeType
                    }

                    val shareIntent: Intent = Intent.createChooser(requestFileIntent, null)
                    startActivityForResult(shareIntent, PICK_FILE_REQUEST_CODE)
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

        dataIntent?.data?.let { dataUri ->
            with(requireActivity()) {
                val file = try {
                    contentResolver.openFileDescriptor(
                        dataUri,
                        "rw"
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

                        contentResolver.openInputStream(dataUri)
                            ?.let { settingsViewModel.importFromFile(it) }
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
                val dataIntent = Intent().apply {
                    action = Intent.ACTION_CREATE_DOCUMENT
                    type = dataFormat.mimeType
                    putExtra(Intent.EXTRA_MIME_TYPES, dataFormat.mimeType)
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
}
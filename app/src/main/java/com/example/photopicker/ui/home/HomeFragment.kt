package com.example.photopicker.ui.home

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.photopicker.ImageUtils
import com.example.photopicker.R
import com.example.photopicker.databinding.FragmentHomeBinding
import timber.log.Timber

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    // private val binding get() = _binding!!

    // Uri returned from takePicture and selectImageFromGallery Intents
    var imageUri: Uri = Uri.EMPTY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        Timber.d("onCreateView")
        return root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pickImageButton.setOnClickListener {
            selectImage()
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun selectImage() {
        // Pixel 2a Android 11 does not have the new PhotoPicker interface.
        ImageUtils.addImageFromDeviceOrCamera(requireActivity(),
            takePictureWithCamera = {
                takePictureWithCamera()
            },
            selectImageFromGallery = {
                //Google's simpler interface, Local files only, no cloud!
                pickOnePhotoFromPhotoPicker()
            })
    }

    /***********************************************
     *
     * Photo Picker (Device/Gallery)
     *
     */
    /**
     * Google's Launch an intent to pick ONE photo.
     * The launcher will handle the Results
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun pickOnePhotoFromPhotoPicker() {
        Timber.d("pickOnePhoto - Build version ${Build.VERSION.SDK_INT}")
        // Launch Google's new photo picker, allowing the user to choose only images.
        // LOCAL FILES ONLY, NO CLOUD!!!
        // PickVisualMediaRequest is a VisualMediaType
        pickOnePhotoFromPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    /**
     * Call Google's PhotoPicker activity launcher (for Result)
     * in select-single-image mode.
     * The new photoPicker doesn't require saving the image
     * to MediageStorage. PhotoPicker picks images from there already.
     * Now we just save its URI, which
     * we also give Read permission so it persists across app sessions.
     */
    private val pickOnePhotoFromPhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media
            // item or closes the photo picker.
            uri?.let { imageUri ->
                Timber.d("PhotoPicker - Selected URI: $uri")
                ImageUtils.displayCircleImageUri(
                    requireContext(),
                    imageUri.toString(),
                    binding.imageView
                )
                // So the uri persists
                //  grantPersistentReadAccess(imageUri)
            } ?: run {
                Timber.d("PhotoPicker - no media selected")
            }
        }

    /***********************************************
     *
     * Take picture with camera
     *
     */
    private fun takePictureWithCamera() {
        lifecycleScope.launchWhenStarted {
            ImageUtils.getTmpFileUri(
                applicationContext = requireActivity().applicationContext,
                requireActivity().cacheDir
            ).let { uri ->
                imageUri = uri
                takePictureActivityResultLauncher.launch(uri)
            }
        }
    }

    // Android returns a URI for a temporary cached file.
    // Create a bitmap and save it to MediaStore (content://media/external/images/media/27)
    // so it will be accessible from any Photos app.
    // https://developer.android.com/training/camera/photobasics
    private val takePictureActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            imageUri.path?.let { path ->
                Timber.d("takePictureActivityResult Path = $path")
                if (isSuccess) {
                    if (imageUri != Uri.EMPTY) {
                        // On later Android versions, the image will be stored in "sdcard/Pictures"
                        Timber.d("Uri from camera $imageUri")
                        binding.imageView.setImageURI(imageUri)
                    }
                }
            }
        }
}
package com.example.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.util.CursorUtil.getColumnIndex
import com.example.criminalintent.PictureUtils.Companion.getScaledBitmap
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val TAG = "CrimeFragment"

class CrimeFragment: Fragment() {

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy{
        ViewModelProvider(this@CrimeFragment).get(CrimeDetailViewModel::class.java)
    }

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var dialSuspectButton: ImageButton
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, getSuspectName())
        }
        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_crime, container, false)

        loadViews(v)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner,
        Observer{ crime ->
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.criminalIntent.fileprovider",
                    photoFile)
                updateUI()
            }
        })
    }

    private val startDialSuspectForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        when{
            result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
            result.data != null -> {
                val contactUri: Uri = result.data?.data!!
                // Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                // Perform your query - the contactUri is like a "where" clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    // Verify cursor contains at least one result
                    if (it.count == 0) {
                        return@registerForActivityResult
                    }
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
        }
    }


    private val startQuerySuspectNameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        when{
            result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
            result.data != null -> {
                val contactUri: Uri = result.data?.data!!
                // Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts._ID)
                // Perform your query - the contactUri is like a "where" clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    // Verify cursor contains at least one result
                    if (it.count == 0) {
                        return@registerForActivityResult
                    }
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspectId = it.getString(0)
                    crime.suspect = suspectId
                    crimeDetailViewModel.saveCrime(crime)

                    suspectButton.text = getSuspectName()
                }
            }
        }
    }

    private fun getSuspectName(): String{
        return getValueFromContentResolverByID(ContactsContract.Contacts.DISPLAY_NAME)
    }

    private fun getSuspectPhoneNumber(): String{
        return getValueFromContentResolverByID(ContactsContract.CommonDataKinds.Phone.NUMBER)
    }

    private fun getValueFromContentResolverByID(field: String): String{
        var requestedValue = ""
        val queryFields = arrayOf(field)
        val cursor = requireActivity().contentResolver
            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, queryFields,
                ContactsContract.Data.CONTACT_ID + "= ? "
                , arrayOf(crime.suspect), null)
        cursor?.apply {
            val index = getColumnIndex(field)
            while(moveToNext()){
                requestedValue = getString(index)
            }
        }

        return requestedValue
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
        ){isGranted ->
            if(isGranted){
                val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                startQuerySuspectNameLauncher.launch(pickContactIntent)
            }else{
                suspectButton.isEnabled = false
            }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){ result ->
        when{
            result.resultCode != Activity.RESULT_OK -> return@registerForActivityResult
            else -> {
                updatePhotoView()
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher{
            override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {
                //Empty on purpose
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                //Empty on purpose
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> crime.isSolved = isChecked }
        }
        parentFragmentManager.setFragmentResultListener(TimePickerFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            onDateAndTimeSelected(bundle.getSerializable(TimePickerFragment.TIME_SELECTED_KEY) as Date)
        }
        dateButton.setOnClickListener {
            DatePickerFragment.getInstance(crime.date).apply {
                show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val intentChooser = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(intentChooser) }
        }
        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                val permissionStatus = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                if(permissionStatus == PackageManager.PERMISSION_GRANTED){
                    startQuerySuspectNameLauncher.launch(pickContactIntent)
                }else{
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                }

            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if(resolvedActivity == null){
                isEnabled = false
            }
        }
        dialSuspectButton.apply {

            setOnClickListener {
                val permissionStatus = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS)
                if(permissionStatus == PackageManager.PERMISSION_GRANTED){
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${getSuspectPhoneNumber()}"))
                    startActivity(dialIntent)
                }else{
                    isEnabled = false
                }
            }
        }
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                takePhotoLauncher.launch(captureImage)
            }
        }
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    private fun loadViews(v: View){
        titleField = v.findViewById(R.id.crime_title)
        dateButton = v.findViewById(R.id.crime_date)
        solvedCheckBox = v.findViewById(R.id.crime_solved)
        reportButton = v.findViewById(R.id.crime_report)
        suspectButton = v.findViewById(R.id.crime_suspect)
        dialSuspectButton = v.findViewById(R.id.dial_suspect)
        photoButton = v.findViewById(R.id.crime_camera)
        photoView = v.findViewById(R.id.crime_photo)
    }

    private fun updateUI(){
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            val permissionStatus = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
            if(permissionStatus == PackageManager.PERMISSION_GRANTED) {
                suspectButton.text = getSuspectName()
                dialSuspectButton.visibility = View.VISIBLE
            }else{
                dialSuspectButton.visibility = View.GONE
            }
        }
        updatePhotoView()
    }

    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{
            val args: Bundle = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }


    private fun onDateAndTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

}
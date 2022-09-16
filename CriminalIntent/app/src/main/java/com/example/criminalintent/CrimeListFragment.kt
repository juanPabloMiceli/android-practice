package com.example.criminalintent

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     */
    interface Callbacks{
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var noCrimesCreated: TextView
    private lateinit var createCrime: FloatingActionButton
    private var adapter: CrimeAdapter? = CrimeAdapter()

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this@CrimeListFragment).get(CrimeListViewModel::class.java)
    }

    private inner class CrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.item_crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.item_crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = SimpleDateFormat("EEEE, dd MMM, yyyy, hh:mm aa", Locale.ROOT).format(this.crime.date)
            solvedImageView.visibility = if (crime.isSolved){
                View.VISIBLE
            }else{
                View.GONE
            }
        }

        override fun onClick(p0: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class DiffUtilCallback: DiffUtil.ItemCallback<Crime>(){
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }
    }

    private inner class CrimeAdapter: ListAdapter<Crime, CrimeHolder>(DiffUtilCallback()){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    private fun createEmptyCrime(){
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.new_crime -> {
                createEmptyCrime()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_crime_list, container, false)

        noCrimesCreated = v.findViewById(R.id.no_crimes_created)
        createCrime = v.findViewById(R.id.new_crime)
        createCrime.setOnClickListener {
            createEmptyCrime()
        }
        crimeRecyclerView = v.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner,
        Observer{ crimes ->
            updateUI(crimes)
        })
    }

    private fun updateUI(crimes: List<Crime>){
        adapter?.submitList(crimes)

        if(crimes.isNotEmpty()){
//            crimeListViewModel.removeCrime(crimes[0])
            noCrimesCreated.visibility = View.GONE
            createCrime.visibility = View.GONE
        }else{
            noCrimesCreated.visibility = View.VISIBLE
            createCrime.visibility = View.VISIBLE
        }

    }

    companion object{
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }
}
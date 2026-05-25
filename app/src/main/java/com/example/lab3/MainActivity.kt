package com.example.lab3

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private val allPosts = mutableListOf<Post>()
    private val displayedPosts = mutableListOf<Post>()
    private lateinit var adapter: PostAdapter

    private val queryCache = mutableMapOf<String, CacheEntry>()

    private val gson = Gson()
    private val PREFS_NAME = "BlogPrefs"
    private val POSTS_KEY = "saved_posts"

    private val CACHE_EXPIRATION_TIME = 600_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPosts()

        val categoryInput = findViewById<EditText>(R.id.categoryInput)
        val titleInput = findViewById<EditText>(R.id.editTextText)
        val contentInput = findViewById<EditText>(R.id.editTextText2)
        val myButton = findViewById<Button>(R.id.button)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val filterInput = findViewById<EditText>(R.id.filterInput)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PostAdapter(displayedPosts,
            onEditClick = { position ->
                val postToEdit = displayedPosts[position]
                categoryInput.setText(postToEdit.category)
                titleInput.setText(postToEdit.title)
                contentInput.setText(postToEdit.content)

                allPosts.remove(postToEdit)
                queryCache.clear()
                filterPosts(searchInput.text.toString(), filterInput.text.toString())
            },
            onDeleteClick = { position ->
                val postToDelete = displayedPosts[position]
                allPosts.remove(postToDelete)
                queryCache.clear()
                filterPosts(searchInput.text.toString(), filterInput.text.toString())
            }
        )
        recyclerView.adapter = adapter

        myButton.setOnClickListener {
            val categoryText = categoryInput.text.toString()
            val titleText = titleInput.text.toString()
            val contentText = contentInput.text.toString()

            if (titleText.isNotEmpty() && contentText.isNotEmpty()) {
                val newPost = Post(titleText, contentText, categoryText)
                allPosts.add(newPost)

                queryCache.clear()
                filterPosts(searchInput.text.toString(), filterInput.text.toString())

                Toast.makeText(this, "Створено пост: $titleText", Toast.LENGTH_SHORT).show()
                categoryInput.text.clear()
                titleInput.text.clear()
                contentInput.text.clear()
            } else {
                Toast.makeText(this, "Будь ласка, заповніть усі поля", Toast.LENGTH_SHORT).show()
            }
        }

        btnSearch.setOnClickListener {
            filterPosts(searchInput.text.toString(), filterInput.text.toString())
        }
    }

    override fun onPause() {
        super.onPause()
        savePosts()
    }

    private fun savePosts() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(allPosts)
        sharedPreferences.edit().putString(POSTS_KEY, json).apply()
    }

    private fun loadPosts() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(POSTS_KEY, null)

        if (json != null) {
            val type = object : TypeToken<MutableList<Post>>() {}.type
            val savedList: MutableList<Post> = gson.fromJson(json, type)

            allPosts.clear()
            allPosts.addAll(savedList)
        }
        displayedPosts.clear()
        displayedPosts.addAll(allPosts)
    }

    private fun filterPosts(searchQuery: String, categoryFilter: String) {
        val query = searchQuery.lowercase()
        val filter = categoryFilter.lowercase()
        val cacheKey = "${query}_${filter}"

        displayedPosts.clear()

        val currentTime = System.currentTimeMillis()
        val cachedEntry = queryCache[cacheKey]

        if (cachedEntry != null && (currentTime - cachedEntry.timestamp) < CACHE_EXPIRATION_TIME) {
            displayedPosts.addAll(cachedEntry.posts)
            Toast.makeText(this, "Завантажено з кешу ⚡", Toast.LENGTH_SHORT).show()
        } else {
            if (cachedEntry != null) {
                queryCache.remove(cacheKey)
            }

            val results = mutableListOf<Post>()
            for (post in allPosts) {
                val matchesSearch = post.title.lowercase().contains(query) || post.content.lowercase().contains(query)
                val matchesCategory = if (filter.isEmpty()) true else post.category.lowercase().contains(filter)

                if (matchesSearch && matchesCategory) {
                    results.add(post)
                }
            }
            displayedPosts.addAll(results)

            queryCache[cacheKey] = CacheEntry(results.toList(), currentTime)
        }
        adapter.notifyDataSetChanged()
    }
}

data class CacheEntry(
    val posts: List<Post>,
    val timestamp: Long
)

data class Post(
    var title: String,
    var content: String,
    var category: String,
    val comments: MutableList<String> = mutableListOf()
)
# CombineWrapperListAdapter
ListAdapter that can combine multiple adapters and puts them in a sequential order.
Adapters can be both ```RecyclerView.Adapter``` and ```ListAdapter```.
There are a few requirements: The Adapters within the CombineWrapperListAdapter all need to implement the ```WrappedListAdapter```-Interface and need to extend either ```ListAdapter\<T, RecyclerView.ViewHolder\>``` or  ```RecyclerView.Adapter\<RecyclerView.ViewHolder\>```. 

## How to implement
First Implement the ```WrappedListAdapter```-interface into the adapters that need to be combined, like this:
```
class WordAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CombineWrapperListAdapter.WrappedListAdapter<String> {
   
       ...
   
       override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
           bindListItemViewHolder(holder, list[position], emptyList())
       }
   
       override fun bindListItemViewHolder(holder: RecyclerView.ViewHolder, item: String, payloads: List<Any>) {
           (holder as WordViewHolder).bind(item)
       }
      
       override fun getItemViewType(position: Int): Int = getItemViewType(list[position])
   
       override fun getItemViewType(item: String): Int = VIEW_TYPE_WORD
   
       override fun getListItems(): List<String> = list
   
       override fun getDiffCallback(): DiffUtil.ItemCallback<String> = diffCallback
   
       ...
   
       companion object {
   
           private const val VIEW_TYPE_WORD: Int = 1
   
           private val diffCallback = object : DiffUtil.ItemCallback<String>() {
               override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
   
               override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = true
           }
       }
   }
```
   
Now the adapters can be added to the CombineWrapperListAdapter:

```
    private val combineWrapperListAdapter = CombineWrapperListAdapter()
    private val wordAdapter: WordAdapter = WordAdapter()
    private val colorAdapter: ColorAdapter = ColorAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
    
        ...
         
        recycler_view.adapter = combineWrapperListAdapter

        combineWrapperListAdapter.add(wordAdapter)
        combineWrapperListAdapter.add(colorAdapter)

        ...
    }
```

Whenever data in the adapters gets updated/removed/changed call: ```notifyAdaptersChanged()``` on the CombineWrapperListAdapter to let the listAdapter calculate the changes and update it's contents.

## References
https://developer.android.com/reference/android/support/v7/recyclerview/extensions/ListAdapter

## License
```
MIT License

Copyright (c) 2017 Tristan Vanderaerden

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

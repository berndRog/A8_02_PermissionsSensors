package de.rogallab.mobile.domain.utilities

class RingBuffer<T>(private val capacity: Int) {
   private val buffer: Array<Any?> = Array(capacity) { null }  // Buffer to hold elements
   private var head = 0  // Points to where the next element will be added
   private var tail = 0  // Points to the current element to be read
   private var size = 0  // Tracks the current number of elements in the buffer

   // Add an element to the buffer
   fun add(element: T) {
      if (size == capacity) {
         // Overwrite the oldest element (move the tail forward)
         tail = (tail + 1) % capacity
      } else {
         size++
      }
      buffer[head] = element
      head = (head + 1) % capacity
   }

   // Remove and return the oldest element from the buffer (FIFO)
   fun remove(): T? {
      if (size == 0) {
         return null // Buffer is empty
      }
      @Suppress("UNCHECKED_CAST")
      val element = buffer[tail] as T
      buffer[tail] = null  // Clear the slot
      tail = (tail + 1) % capacity
      size--
      return element
   }

   // Check if the buffer is empty
   fun isEmpty(): Boolean {
      return size == 0
   }

   // Check if the buffer is full
   fun isFull(): Boolean {
      return size == capacity
   }

   // Get the current size of the buffer
   fun getSize(): Int {
      return size
   }

   // Get the capacity of the buffer
   fun getCapacity(): Int {
      return capacity
   }

   // Peek the oldest element (without removing it)
   fun peek(): T? {
      @Suppress("UNCHECKED_CAST")
      return if (size == 0) null
      else buffer[tail] as T
   }

   fun toList(): List<T> {
      val result = mutableListOf<T>()
      var index = tail

      for (i in 0 until size) {
         @Suppress("UNCHECKED_CAST")
         result.add(buffer[index] as T)
         index = (index + 1) % capacity  // Move to the next index, wrap around if necessary
      }

      return result
   }

}

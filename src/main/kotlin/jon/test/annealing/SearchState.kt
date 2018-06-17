package jon.test.annealing

interface SearchState<T : SearchState<T>> {
    fun step(): T
}

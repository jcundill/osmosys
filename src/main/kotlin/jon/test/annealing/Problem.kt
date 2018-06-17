package jon.test.annealing


interface Problem<T : SearchState<T>> {
    @Throws(InfeasibleProblemException::class)
    fun initialState(): T

    fun energy(searchState: T): Double
}

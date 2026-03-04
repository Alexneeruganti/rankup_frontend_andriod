package com.simats.rankup.technical

data class TechQuestion(
    val id: Int,
    val question: String,
    val answer: String,
    val topic: String
)

object TechnicalRepository {

    fun getQuestions(topic: String): List<TechQuestion> {
        return allQuestions.filter { it.topic == topic }.shuffled()
    }

    private val allQuestions = listOf(
        // Java
        TechQuestion(1, "What is the difference between JDK, JRE, and JVM?", "JDK is the development kit, JRE is the runtime environment, and JVM is the virtual machine that executes bytecode.", "Java"),
        TechQuestion(2, "Explain the concept of Polymorphism in Java.", "Polymorphism allows objects to be treated as instances of their parent class. Types include Compile-time (Overloading) and Runtime (Overriding).", "Java"),
        TechQuestion(3, "What is a Marker Interface?", "An interface with no methods (e.g., Serializable, Cloneable) used to signal the JVM to perform some operation.", "Java"),
        
        // C++
        TechQuestion(4, "What are Virtual Functions in C++?", "Functions declared in a base class that can be overridden in derived classes to achieve runtime polymorphism.", "C++"),
        TechQuestion(5, "Difference between malloc and new?", "malloc allocates memory but doesn't call constructor. new allocates memory and calls the constructor.", "C++"),

        // DBMS
        TechQuestion(6, "What is Normalization?", "The process of organizing data to reduce redundancy and improve data integrity (1NF, 2NF, 3NF, BCNF).", "DBMS"),
        TechQuestion(7, "ACID properties in DBMS?", "Atomicity, Consistency, Isolation, Durability.", "DBMS"),

        // OS
        TechQuestion(8, "What is a Deadlock?", "A situation where a set of processes are blocked because each process is holding a resource and waiting for another resource acquired by some other process.", "Operating Systems"),
        TechQuestion(9, "Difference between Process and Thread?", "Process is a program in execution with its own memory. Thread is a lightweight process sharing memory with other threads of the same process.", "Operating Systems"),

        // CN
        TechQuestion(10, "What is the OSI Model?", "The Open Systems Interconnection model characterizes communication functions into 7 layers: Physical, Data Link, Network, Transport, Session, Presentation, Application.", "Computer Networks"),
        TechQuestion(11, "Difference between TCP and UDP?", "TCP is connection-oriented and reliable. UDP is connectionless and faster but unreliable.", "Computer Networks")
    )
}

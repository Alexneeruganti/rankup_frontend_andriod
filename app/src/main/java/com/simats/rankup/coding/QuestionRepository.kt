package com.simats.rankup.coding

object QuestionRepository {

    fun generateQuestion(difficulty: String): CodingQuestion {
        return when (difficulty) {
            "Easy" -> easyQuestions.random()
            "Medium" -> mediumQuestions.random()
            "Hard" -> hardQuestions.random()
            else -> easyQuestions.random()
        }
    }

    private val easyQuestions = listOf(
        CodingQuestion(
            id = "E1",
            title = "Sum of Two Numbers",
            description = "Write a program that takes two integers as input and prints their sum.",
            difficulty = "Easy",
            constraints = "1 <= A, B <= 1000",
            inputFormat = "Two space-separated integers on a single line.",
            testCases = listOf(
                TestCase("5 10", "15"),
                TestCase("100 200", "300")
            )
        ),
        CodingQuestion(
            id = "E2",
            title = "Check Even or Odd",
            description = "Write a program to check if a number is even or odd.",
            difficulty = "Easy",
            constraints = "1 <= N <= 10^9",
            inputFormat = "A single integer N.",
            testCases = listOf(
                TestCase("4", "Even"),
                TestCase("7", "Odd")
            )
        )
    )

    private val mediumQuestions = listOf(
        CodingQuestion(
            id = "M1",
            title = "Factorial of a Number",
            description = "Write a program to find the factorial of a given number N.",
            difficulty = "Medium",
            constraints = "0 <= N <= 12",
            inputFormat = "A single integer N.",
            testCases = listOf(
                TestCase("5", "120"),
                TestCase("0", "1")
            )
        ),
        CodingQuestion(
            id = "M2",
            title = "Reverse a String",
            description = "Write a program to reverse a given string.",
            difficulty = "Medium",
            constraints = "1 <= |S| <= 100",
            inputFormat = "A single string S.",
            testCases = listOf(
                TestCase("hello", "olleh"),
                TestCase("rankup", "puknar")
            )
        )
    )

    private val hardQuestions = listOf(
        CodingQuestion(
            id = "H1",
            title = "N-th Fibonacci Number",
            description = "Write a program to find the N-th Fibonacci number. F(0)=0, F(1)=1.",
            difficulty = "Hard",
            constraints = "0 <= N <= 30",
            inputFormat = "A single integer N.",
            testCases = listOf(
                TestCase("10", "55"),
                TestCase("6", "8")
            )
        )
    )
}

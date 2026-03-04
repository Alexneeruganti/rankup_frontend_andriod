package com.simats.rankup.coding

data class PistonExclude(
    val language: String,
    val version: String,
    val files: List<PistonFile>,
    val stdin: String = "",
    val args: List<String> = emptyList(),
    val compile_timeout: Int = 10000,
    val run_timeout: Int = 3000
)

data class PistonFile(
    val name: String = "solution",
    val content: String
)

data class PistonResponse(
    val run: PistonRunOutput
)

data class PistonRunOutput(
    val stdout: String,
    val stderr: String,
    val output: String,
    val code: Int,
    val signal: String?
)

data class CodingQuestion(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val constraints: String,
    val inputFormat: String,
    val videoSolutionUrl: String? = null,
    val testCases: List<TestCase>
)

data class TestCase(
    val input: String,
    val expectedOutput: String,
    val isHidden: Boolean = false
)

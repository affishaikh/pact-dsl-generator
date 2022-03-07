package com.github.affishaikh.kotlinbuildergenerator.action

import com.github.affishaikh.kotlinbuildergenerator.services.FileService
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeneratePactDslTest {
    private val generatePactDslIntention = GeneratePactDsl()
    private val ideaTestFixtureFactory = IdeaTestFixtureFactory.getFixtureFactory()
    private lateinit var fixture: CodeInsightTestFixture

    @Before
    fun setUp() {
        clearAllMocks()
        mockkConstructor(FileService::class)
        justRun {
            anyConstructed<FileService>().createFile(any(), any())
        }
        val projectDescriptor = LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
        val fixtureBuilder = ideaTestFixtureFactory.createLightFixtureBuilder(projectDescriptor)
        fixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(
            fixtureBuilder.fixture,
            LightTempDirTestFixtureImpl(true)
        )
        fixture.setUp()
    }

    @After
    fun tearDown() {
        clearAllMocks()
        fixture.tearDown()
    }

    @Test
    fun `should create the pact dsl for a simple class`() {
        val testClass = """
            package com.github.affishaikh.kotlinbuildergenerator.domain
            
            data class Response(
                val name: String = "",<caret>
                val expires_in: Long,
                val type: String = "",
                val manufactured_on: Long,
                val scope: String = "",
                val refresh_token: String,
                val refresh_token_expires_in: Int
            )
        """.trimIndent()

        val pactDsl = """
            PactDslJsonBody()
            .stringType("name")
            .numberType("expires_in")
            .stringType("type")
            .numberType("manufactured_on")
            .stringType("scope")
            .stringType("refresh_token")
            .numberType("refresh_token_expires_in")
            
        """.trimIndent()

        verifyIntentionResults(pactDsl, mapOf("Response.kt" to testClass))
    }

    @Test
    fun `should create the pact dsl for a class having composite classes`() {
        val testClass = """
            data class SuccessResponse(
                val data: SuccessDetails,<caret>
                val refno: String
            )

            data class SuccessDetails(
                val data: String,
                val status: String
            )
        """.trimIndent()

        val pactDsl = """
            PactDslJsonBody()
            .`object`("data")
            .stringType("data")
            .stringType("status")
            .closeObject()
            .asBody()
            .stringType("refno")
            
        """.trimIndent()

        verifyIntentionResults(pactDsl, mapOf("SuccessResponse.kt" to testClass))
    }

    @Test
    fun `should create the pact dsl for a class having an array of objects`() {
        val testClass = """
            data class ObjWithArray(
                val str: String,<caret>
                val arr: List<SomeObj>
            )

            data class SomeObj(
                val a: String,
                val b: String
            )
        """.trimIndent()

        val pactDsl = """
            PactDslJsonBody()
            .stringType("str")
            .minArrayLike("arr", 1)
            .stringType("a")
            .stringType("b")
            .closeArray()
            .asBody()
            
        """.trimIndent()

        verifyIntentionResults(pactDsl, mapOf("ObjWithArray.kt" to testClass))
    }

    @Test
    fun `should create the pact dsl for a class having an array of string`() {
        val testClass = """
            data class ObjWithArray(
                val str: String,
                val arr: List<String>
            )
        """.trimIndent()

        val pactDsl = """
            PactDslJsonBody()
            .stringType("str")
            .array("arr")
            .stringType()
            .closeArray()
            .asBody()
            
        """.trimIndent()

        verifyIntentionResults(pactDsl, mapOf("ObjWithArray.kt" to testClass))
    }

    private fun verifyIntentionResults(expectedBuilder: String, testClasses: Map<String, String>) {
        testClasses.map {
            fixture.configureByText(it.key, it.value)
        }
        fixture.launchAction(generatePactDslIntention)
        val builderSlot = slot<String>()

        verify(exactly = 1) {
            anyConstructed<FileService>().createFile(any(), capture(builderSlot))
        }

        builderSlot.captured shouldBe expectedBuilder
    }
}

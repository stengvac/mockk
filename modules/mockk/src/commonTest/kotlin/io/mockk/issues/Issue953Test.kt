package io.mockk.issues

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * See issue #953
 * Unable to mock vararg methods where the last parameter is not variable
 */
class Issue953Test {

    class TestedClass {

        suspend fun <T> suspendWithLambda(vararg names: String, action: suspend () -> T): T {
            return action()
        }

        suspend fun suspendWithOtherArgs(vararg names: String, otherArg: Int, otherArg2: Boolean): String {
            TODO("not implemented for showcase")
        }
    }

    @Test
    fun `coEvery allow to mock vararg methods with other argument types`() {
        val testedClass = mockk<TestedClass>()
        coEvery {
            testedClass.suspendWithLambda(names = anyVararg(), action = any<suspend () -> Number>())
        } coAnswers {
            // Note lastArg is not correct here as it contains coroutine continuation
            arg<suspend () -> Number>(1)()
        }

        runBlocking {
            val returnedInt = testedClass.suspendWithLambda("a", "b") { 99 }
            assertEquals(returnedInt, 99)
        }

        coEvery {
            testedClass.suspendWithOtherArgs(names = anyVararg(), otherArg = 7, otherArg2 = false)
        } coAnswers { "success" }

        runBlocking {
            val returnedString = testedClass.suspendWithOtherArgs("a", "b", otherArg = 7, otherArg2 = false)
            assertEquals(returnedString, "success")
        }
    }
}

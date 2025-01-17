/**
 * Copyright 2022 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.shreyaspatil.mutekt.codegen.codebuild.mutableModel.impl

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import dev.shreyaspatil.mutekt.codegen.codebuild.ext.eachToParameter

/**
 * Generates a top level function for creating instance of mutable model interface
 */
class MutableModelFactoryFunctionBuilder(
    private val mutableInterfaceName: ClassName,
    private val mutableImplClassName: ClassName,
    private val publicProperties: Sequence<KSPropertyDeclaration>
) {
    // Function name should be same as interface name
    private val functionName = mutableInterfaceName.simpleName

    fun build() = FunSpec.builder(functionName)
        .apply {
            addKdoc(
                CodeBlock.builder()
                    .addStatement("Creates an instance of state model [%T]", mutableInterfaceName)
                    .apply {
                        publicProperties
                            .mapNotNull { prop -> prop.docString?.let { doc -> prop.simpleName.asString() to doc } }
                            .forEach { (param, doc) -> addStatement("@param %L %L", param, doc) }
                    }.build()
            )
        }
        .addModifiers(KModifier.PUBLIC)
        .addParameters(publicProperties.eachToParameter().toList())
        .returns(mutableInterfaceName)
        .addStatement(
            "return %L(%L)",
            mutableImplClassName.simpleName,
            publicProperties.joinToString { it.simpleName.asString() }
        ).build()
}

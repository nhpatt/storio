package com.pushtorefresh.storio.sqlite.annotations.processor.generate;

import com.pushtorefresh.storio.sqlite.annotations.processor.introspection.StorIOSQLiteTypeMeta;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.pushtorefresh.storio.sqlite.annotations.processor.generate.Common.ANDROID_NON_NULL_ANNOTATION_CLASS_NAME;
import static com.pushtorefresh.storio.sqlite.annotations.processor.generate.Common.INDENT;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public class DeleteResolverGenerator {

    @NotNull
    public JavaFile generateJavaFile(@NotNull StorIOSQLiteTypeMeta storIOSQLiteTypeMeta) {
        final ClassName storIOSQLiteTypeClassName = ClassName.get(storIOSQLiteTypeMeta.packageName, storIOSQLiteTypeMeta.simpleName);

        final TypeSpec deleteResolver = TypeSpec.classBuilder(storIOSQLiteTypeMeta.simpleName + "StorIOSQLiteDeleteResolver")
                .addJavadoc("Generated resolver for Delete Operation\n")
                .addModifiers(PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get("com.pushtorefresh.storio.sqlite.operations.delete", "DefaultDeleteResolver"), storIOSQLiteTypeClassName))
                .addMethod(createMapToDeleteQueryMethodSpec(storIOSQLiteTypeMeta, storIOSQLiteTypeClassName))
                .build();

        return JavaFile
                .builder(storIOSQLiteTypeMeta.packageName, deleteResolver)
                .indent(INDENT)
                .build();
    }

    @NotNull
    MethodSpec createMapToDeleteQueryMethodSpec(@NotNull StorIOSQLiteTypeMeta storIOSQLiteTypeMeta, @NotNull ClassName storIOSQLiteTypeClassName) {
        final Map<String, String> where = QueryGenerator.createWhere(storIOSQLiteTypeMeta, "object");

        return MethodSpec.methodBuilder("mapToDeleteQuery")
                .addJavadoc("{@inheritDoc}\n")
                .addAnnotation(Override.class)
                .addAnnotation(ANDROID_NON_NULL_ANNOTATION_CLASS_NAME)
                .addModifiers(PROTECTED)
                .returns(ClassName.get("com.pushtorefresh.storio.sqlite.queries", "DeleteQuery"))
                .addParameter(ParameterSpec.builder(ClassName.get("com.pushtorefresh.storio.sqlite", "StorIOSQLite"), "storIOSQLite")
                        .addAnnotation(ANDROID_NON_NULL_ANNOTATION_CLASS_NAME)
                        .build())
                .addParameter(ParameterSpec.builder(storIOSQLiteTypeClassName, "object")
                    .addAnnotation(ANDROID_NON_NULL_ANNOTATION_CLASS_NAME)
                    .build())
                .addCode("return DeleteQuery.builder()\n" +
                                INDENT + ".table($S)\n" +
                                INDENT + ".where($S)\n" +
                                INDENT + ".whereArgs($L)\n" +
                                INDENT + ".build();\n",
                        storIOSQLiteTypeMeta.storIOSQLiteType.table(),
                        where.get(QueryGenerator.WHERE_CLAUSE),
                        where.get(QueryGenerator.WHERE_ARGS))
                .build();
    }
}

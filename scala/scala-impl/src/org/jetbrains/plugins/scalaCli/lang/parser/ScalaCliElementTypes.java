package org.jetbrains.plugins.scalaCli.lang.parser;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scalaCli.psi.impl.ScCliDirectiveImpl;
import org.jetbrains.plugins.scalaCli.ScalaCliLanguage;

public interface ScalaCliElementTypes {

    @NotNull ILazyParseableElementType SCALA_CLI_DIRECTIVE = new ILazyParseableElementType("SCALA_CLI_DIRECTIVE", ScalaCliLanguage.INSTANCE) {

        @Override
        @Nullable
        public ASTNode parseContents(@NotNull ASTNode lazyNode) {
            Project project = JavaPsiFacade.getInstance(lazyNode.getTreeParent().getPsi().getProject()).getProject();

            Language language = getLanguage();
            ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);

            PsiBuilder builder = PsiBuilderFactory.getInstance()
                    .createBuilder(project, lazyNode, parserDefinition.createLexer(project), language, lazyNode.getText());

            ASTNode result = parserDefinition.createParser(project).parse(this, builder);

            return result.getFirstChildNode();
        }

        @Nullable
        @Override
        public ASTNode createNode(CharSequence text) {
            return text != null ? new ScCliDirectiveImpl(text, this) : null;
        }
    };
}

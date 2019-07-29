// This is a generated file. Not intended for manual editing.
package dev.blachut.svelte.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.JSEmbeddedContent;
import com.intellij.lang.javascript.psi.JSExecutionScope;
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SvelteCodeFragmentImpl extends JSStubElementImpl<JSEmbeddedContentStub> implements JSExecutionScope {
    public SvelteCodeFragmentImpl(ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        boolean result = true;
        List<PsiFile> psiFiles = this.getContainingFile().getViewProvider().getAllFiles();
        for (PsiFile psiFile : psiFiles) {
            Collection<JSEmbeddedContent> jsEmbeddedContents = PsiTreeUtil.findChildrenOfType(psiFile, JSEmbeddedContent.class);
            for (JSEmbeddedContent jsEmbeddedContent: jsEmbeddedContents) {
                result &= JSResolveUtil.processDeclarationsInScope(jsEmbeddedContent, processor, state, lastParent, place);
            }
        }

        return result;
    }
}

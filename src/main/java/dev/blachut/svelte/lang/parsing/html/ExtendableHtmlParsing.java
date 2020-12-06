// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

/*
 * @author max
 */
package dev.blachut.svelte.lang.parsing.html;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.daemon.XmlErrorBundle;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.html.HtmlParsing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.ICustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.containers.Stack;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created because HtmlParsing is not extendable enough to support Svelte overrides
 */
public class ExtendableHtmlParsing extends HtmlParsing {
    @NonNls
    private static final String TR_TAG = "tr";
    @NonNls
    private static final String TD_TAG = "td";
    @NonNls
    private static final String TH_TAG = "th";
    @NonNls
    private static final String TABLE_TAG = "table";

    private final PsiBuilder myBuilder;
    private final Stack<String> myTagNamesStack = new Stack<>();
    private final Stack<String> myOriginalTagNamesStack = new Stack<>();
    private final Stack<PsiBuilder.Marker> myTagMarkersStack = new Stack<>();
    @NonNls
    private static final String COMPLETION_NAME = StringUtil.toLowerCase(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED);

    public ExtendableHtmlParsing(final PsiBuilder builder) {
        super(builder);
        myBuilder = builder;
    }

    public void parseDocument() {
        final PsiBuilder.Marker document = mark();

        while (token() == XmlTokenType.XML_COMMENT_START) {
            parseComment();
        }

        parseProlog();

        PsiBuilder.Marker error = null;
        while (!eof()) {
            final IElementType tt = token();
            if (tt == XmlTokenType.XML_START_TAG_START) {
                error = flushError(error);
                parseTag();
            } else if (tt == XmlTokenType.XML_COMMENT_START) {
                error = flushError(error);
                parseComment();
            } else if (tt == XmlTokenType.XML_PI_START) {
                error = flushError(error);
                parseProcessingInstruction();
            } else if (tt == XmlTokenType.XML_CHAR_ENTITY_REF || tt == XmlTokenType.XML_ENTITY_REF_TOKEN) {
                parseReference();
            } else if (tt == XmlTokenType.XML_REAL_WHITE_SPACE || tt == XmlTokenType.XML_DATA_CHARACTERS) {
                error = flushError(error);
                advance();
            } else if (tt == XmlTokenType.XML_END_TAG_START) {
                final PsiBuilder.Marker tagEndError = myBuilder.mark();

                advance();
                if (token() == XmlTokenType.XML_NAME) {
                    advance();
                    if (token() == XmlTokenType.XML_TAG_END) {
                        advance();
                    }
                }

                tagEndError.error(XmlErrorBundle.message("xml.parsing.closing.tag.matches.nothing"));
            } else if (hasCustomTopLevelContent()) {
                error = parseCustomTopLevelContent(error);
            } else {
                if (error == null) error = mark();
                advance();
            }
        }

        flushOpenTags();
        myTagMarkersStack.clear();
        myTagNamesStack.clear();
        myOriginalTagNamesStack.clear();

        if (error != null) {
            error.error(XmlErrorBundle.message("top.level.element.is.not.completed"));
        }

        document.done(XmlElementType.HTML_DOCUMENT);
    }

    protected void flushOpenTags() {
        while (hasTags()) {
            final String tagName = myTagNamesStack.peek();
            if (isEndTagRequired(tagName)) {
                error(XmlErrorBundle.message("named.element.is.not.closed", myOriginalTagNamesStack.peek()));
            }
            doneTag();
        }
    }

    protected boolean isEndTagRequired(@NotNull String tagName) {
        return !HtmlUtil.isOptionalEndForHtmlTagL(tagName) && !"html".equals(tagName) && !"body".equals(tagName);
    }

    protected boolean hasCustomTopLevelContent() {
        return false;
    }

    protected PsiBuilder.Marker parseCustomTopLevelContent(PsiBuilder.Marker error) {
        return error;
    }

    protected boolean hasCustomTagContent() {
        return false;
    }

    protected PsiBuilder.Marker parseCustomTagContent(PsiBuilder.Marker xmlText) {
        return xmlText;
    }

    protected boolean hasCustomHeaderContent() {
        return false;
    }

    protected void parseCustomHeaderContent() {
    }

    @Nullable
    protected static PsiBuilder.Marker flushError(PsiBuilder.Marker error) {
        if (error != null) {
            error.error(XmlErrorBundle.message("xml.parsing.unexpected.tokens"));
        }
        return null;
    }

    private void parseDoctype() {
        assert token() == XmlTokenType.XML_DOCTYPE_START : "Doctype start expected";
        final PsiBuilder.Marker doctype = mark();
        advance();

        while (token() != XmlTokenType.XML_DOCTYPE_END && !eof()) advance();
        if (eof()) {
            error(XmlErrorBundle.message("xml.parsing.unexpected.end.of.file"));
        } else {
            advance();
        }

        doctype.done(XmlElementType.XML_DOCTYPE);
    }

    public void parseTag() {
        assert token() == XmlTokenType.XML_START_TAG_START : "Tag start expected";
        String originalTagName;
        PsiBuilder.Marker xmlText = null;
        while (!eof()) {
            final IElementType tt = token();
            if (tt == XmlTokenType.XML_START_TAG_START) {
                xmlText = terminateText(xmlText);
                final PsiBuilder.Marker tag = mark();

                // Start tag header
                advance();
                if (token() != XmlTokenType.XML_NAME) {
                    error(XmlErrorBundle.message("xml.parsing.tag.name.expected"));
                    originalTagName = "";
                } else {
                    originalTagName = Objects.requireNonNull(myBuilder.getTokenText());
                    advance();
                }

                String tagName = StringUtil.toLowerCase(originalTagName);
                while (childTerminatesParentInStack(tagName)) {
                    PsiBuilder.Marker top = closeTag();
                    top.doneBefore(getHtmlTagElementType(), tag);
                }

                pushTag(tag, tagName, originalTagName);

                parseHeader(tagName);

                if (token() == XmlTokenType.XML_EMPTY_ELEMENT_END) {
                    advance();
                    doneTag();
                    continue;
                }

                if (token() == XmlTokenType.XML_TAG_END) {
                    advance();
                } else {
                    error(XmlErrorBundle.message("tag.start.is.not.closed"));
                    doneTag();
                    continue;
                }

                if (isSingleTag(tagName, originalTagName)) {
                    final PsiBuilder.Marker footer = mark();
                    while (token() == XmlTokenType.XML_REAL_WHITE_SPACE) {
                        advance();
                    }
                    if (token() == XmlTokenType.XML_END_TAG_START) {
                        advance();
                        if (token() == XmlTokenType.XML_NAME) {
                            if (tagName.equalsIgnoreCase(myBuilder.getTokenText())) {
                                advance();
                                footer.drop();
                                if (token() == XmlTokenType.XML_TAG_END) {
                                    advance();
                                }
                                doneTag();
                                continue;
                            }
                        }
                    }
                    footer.rollbackTo();
                    doneTag();
                }
            } else if (tt == XmlTokenType.XML_PI_START) {
                xmlText = terminateText(xmlText);
                parseProcessingInstruction();
            } else if (tt == XmlTokenType.XML_ENTITY_REF_TOKEN || tt == XmlTokenType.XML_CHAR_ENTITY_REF) {
                xmlText = startText(xmlText);
                parseReference();
            } else if (tt == XmlTokenType.XML_CDATA_START) {
                xmlText = startText(xmlText);
                parseCData();
            } else if (tt == XmlTokenType.XML_COMMENT_START) {
                xmlText = startText(xmlText);
                parseComment();
            } else if (tt == XmlTokenType.XML_BAD_CHARACTER) {
                xmlText = startText(xmlText);
                final PsiBuilder.Marker error = mark();
                advance();
                error.error(XmlErrorBundle.message("unescaped.ampersand.or.nonterminated.character.entity.reference"));
            } else if (tt instanceof ICustomParsingType || tt instanceof ILazyParseableElementType) {
                xmlText = terminateText(xmlText);
                advance();
            } else if (token() == XmlTokenType.XML_END_TAG_START) {
                xmlText = terminateText(xmlText);
                final PsiBuilder.Marker footer = mark();
                advance();

                if (token() == XmlTokenType.XML_NAME) {
                    String endName = StringUtil.toLowerCase(Objects.requireNonNull(myBuilder.getTokenText()));
                    final String parentTagName = !myTagNamesStack.isEmpty() ? myTagNamesStack.peek() : "";
                    if (!parentTagName.equals(endName) && !endName.endsWith(COMPLETION_NAME)) {
                        final boolean isOptionalTagEnd = HtmlUtil.isOptionalEndForHtmlTagL(parentTagName);
                        final boolean hasChancesToMatch = HtmlUtil.isOptionalEndForHtmlTagL(endName) ? childTerminatesParentInStack(endName) : isTagNameFurtherInStack(endName);
                        if (hasChancesToMatch) {
                            footer.rollbackTo();
                            if (!isOptionalTagEnd) {
                                error(XmlErrorBundle.message("named.element.is.not.closed", myOriginalTagNamesStack.peek()));
                            }
                            doneTag();
                        } else {
                            advance();
                            if (token() == XmlTokenType.XML_TAG_END) advance();
                            footer.error(XmlErrorBundle.message("xml.parsing.closing.tag.matches.nothing"));
                        }
                        continue;
                    }

                    advance();

                    while (token() != XmlTokenType.XML_TAG_END && token() != XmlTokenType.XML_START_TAG_START && token() != XmlTokenType.XML_END_TAG_START && !eof()) {
                        error(XmlErrorBundle.message("xml.parsing.unexpected.token"));
                        advance();
                    }
                } else {
                    // error(XmlErrorBundle.message("xml.parsing.closing.tag.name.missing"));
                    error("Closing tag name missing");
                }
                footer.drop();

                if (token() == XmlTokenType.XML_TAG_END) {
                    advance();
                } else {
                    error(XmlErrorBundle.message("xml.parsing.closing.tag.is.not.done"));
                }

                if (hasRealTags()) {
                    doneTag();
                }
            } else if ((token() == XmlTokenType.XML_REAL_WHITE_SPACE || token() == XmlTokenType.XML_DATA_CHARACTERS) && !hasTags()) {
                xmlText = terminateText(xmlText);
                advance();
            } else if (hasCustomTagContent()) {
                xmlText = parseCustomTagContent(xmlText);
            } else {
                xmlText = startText(xmlText);
                advance();
            }
        }
        terminateText(xmlText);
    }

    protected boolean isSingleTag(@NotNull String tagName, @NotNull String originalTagName) {
        return HtmlUtil.isSingleHtmlTagL(tagName);
    }

    protected boolean hasTags() {
        return !myTagNamesStack.isEmpty();
    }

    protected boolean hasRealTags() {
        // TODO try to merge back with hasTags
        return hasTags();
    }

    protected void pushTag(PsiBuilder.Marker tagMarker, String tagName, String originalTagName) {
        myTagMarkersStack.push(tagMarker);
        myTagNamesStack.push(tagName);
        myOriginalTagNamesStack.push(originalTagName);
    }

    protected PsiBuilder.Marker closeTag() {
        myTagNamesStack.pop();
        myOriginalTagNamesStack.pop();
        return myTagMarkersStack.pop();
    }

    protected String peekTagName() {
        return myTagNamesStack.peek();
    }

    protected PsiBuilder.Marker peekTagMarker() {
        return myTagMarkersStack.peek();
    }

    protected int tagLevel() {
        return myTagNamesStack.size();
    }

    protected boolean isTagNameFurtherInStack(@NotNull String endName) {
        return myTagNamesStack.contains(endName);
    }

    private void doneTag() {
        PsiBuilder.Marker tag = myTagMarkersStack.peek();
        tag.done(getHtmlTagElementType());
        final String tagName = myTagNamesStack.peek();
        closeTag();

        terminateAutoClosingParentTag(tag, tagName);
    }

    /**
     * Handles things like {@code <p>parentTag<p>tag} which result in 2 sibling paragraphs
     */
    protected void terminateAutoClosingParentTag(@NotNull PsiBuilder.Marker tag, @NotNull String tagName) {
        final String parentTagName = hasTags() ? myTagNamesStack.peek() : "";
        boolean isInlineTagContainer = HtmlUtil.isInlineTagContainerL(parentTagName);
        boolean isOptionalTagEnd = HtmlUtil.isOptionalEndForHtmlTagL(parentTagName);
        boolean isValidParent = isInlineTagContainer && isOptionalTagEnd;

        if (isValidParent && HtmlUtil.isHtmlBlockTagL(tagName) && !HtmlUtil.isPossiblyInlineTag(tagName)) {
            PsiBuilder.Marker top = closeTag();
            top.doneBefore(getHtmlTagElementType(), tag);
        }
    }

    protected IElementType getHtmlTagElementType() {
        return XmlElementType.HTML_TAG;
    }

    private void parseHeader(String tagName) {
        boolean freeMakerTag = !tagName.isEmpty() && '#' == tagName.charAt(0);

        do {
            final IElementType tt = token();
            if (freeMakerTag) {
                if (tt == XmlTokenType.XML_EMPTY_ELEMENT_END ||
                    tt == XmlTokenType.XML_TAG_END ||
                    tt == XmlTokenType.XML_END_TAG_START ||
                    tt == XmlTokenType.XML_START_TAG_START) break;
                advance();
            } else {
                if (tt == XmlTokenType.XML_NAME) {
                    parseAttribute();
                } else if (tt == XmlTokenType.XML_CHAR_ENTITY_REF || tt == XmlTokenType.XML_ENTITY_REF_TOKEN) {
                    parseReference();
                } else if (hasCustomHeaderContent()) {
                    parseCustomHeaderContent();
                } else {
                    break;
                }
            }
        }
        while (!eof());
    }

    private boolean childTerminatesParentInStack(final String childName) {
        for (int i = myTagNamesStack.size() - 1; i >= 0; i--) {
            String parentName = myTagNamesStack.get(i);

            Boolean result = childTerminatesParent(childName, parentName, i + 1);
            if (result != null) {
                return result;
            }
        }
        return false;
    }

    @Nullable
    protected Boolean childTerminatesParent(final String childName, final String parentName, int tagLevel) {
        final boolean isCell = TD_TAG.equals(childName) || TH_TAG.equals(childName);
        final boolean isRow = TR_TAG.equals(childName);
        final boolean isStructure = isStructure(childName);

        final boolean isParentTable = TABLE_TAG.equals(parentName);
        final boolean isParentStructure = isStructure(parentName);

        if (isCell && (TR_TAG.equals(parentName) || isParentStructure || isParentTable) ||
            isRow && (isParentStructure || isParentTable) ||
            isStructure && isParentTable) {
            return false;
        }

        if ("li".equals(childName) && ("ul".equals(parentName) || "ol".equals(parentName))) {
            return false;
        }

        if ("dl".equals(parentName) && ("dd".equals(childName) || "dt".equals(childName))) {
            return false;
        }

        if (HtmlUtil.canTerminate(childName, parentName)) {
            return true;
        }

        return null;
    }

    private static boolean isStructure(String childName) {
        return "thead".equals(childName) || "tbody".equals(childName) || "tfoot".equals(childName);
    }

    @NotNull
    protected PsiBuilder.Marker startText(@Nullable PsiBuilder.Marker xmlText) {
        if (xmlText == null) {
            xmlText = mark();
        }
        return xmlText;
    }

    @Nullable
    protected static PsiBuilder.Marker terminateText(@Nullable PsiBuilder.Marker xmlText) {
        if (xmlText != null) {
            xmlText.done(XmlElementType.XML_TEXT);
            xmlText = null;
        }
        return xmlText;
    }

    protected void parseCData() {
        assert token() == XmlTokenType.XML_CDATA_START;
        final PsiBuilder.Marker cdata = mark();
        while (token() != XmlTokenType.XML_CDATA_END && !eof()) {
            advance();
        }

        if (!eof()) {
            advance();
        }

        cdata.done(XmlElementType.XML_CDATA);
    }

    protected void parseComment() {
        final PsiBuilder.Marker comment = mark();
        advance();
        while (true) {
            final IElementType tt = token();
            if (tt == XmlTokenType.XML_COMMENT_CHARACTERS || tt == XmlTokenType.XML_CONDITIONAL_COMMENT_START
                || tt == XmlTokenType.XML_CONDITIONAL_COMMENT_START_END || tt == XmlTokenType.XML_CONDITIONAL_COMMENT_END_START
                || tt == XmlTokenType.XML_CONDITIONAL_COMMENT_END) {
                advance();
                continue;
            }
            if (tt == XmlTokenType.XML_ENTITY_REF_TOKEN || tt == XmlTokenType.XML_CHAR_ENTITY_REF) {
                parseReference();
                continue;
            }
            if (tt == XmlTokenType.XML_BAD_CHARACTER) {
                final PsiBuilder.Marker error = mark();
                advance();
                error.error(XmlErrorBundle.message("xml.parsing.bad.character"));
                continue;
            }
            if (tt == XmlTokenType.XML_COMMENT_END) {
                advance();
            }
            break;
        }
        comment.done(XmlElementType.XML_COMMENT);
    }

    protected void parseReference() {
        if (token() == XmlTokenType.XML_CHAR_ENTITY_REF) {
            advance();
        } else if (token() == XmlTokenType.XML_ENTITY_REF_TOKEN) {
            final PsiBuilder.Marker ref = mark();
            advance();
            ref.done(XmlElementType.XML_ENTITY_REF);
        } else {
            assert false : "Unexpected token";
        }
    }

    protected void parseAttribute() {
        assert token() == XmlTokenType.XML_NAME;
        final PsiBuilder.Marker att = mark();
        advance();
        if (token() == XmlTokenType.XML_EQ) {
            advance();
            parseAttributeValue();
        }
        att.done(XmlElementType.XML_ATTRIBUTE);
    }

    protected void parseAttributeValue() {
        final PsiBuilder.Marker attValue = mark();
        if (token() == XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER) {
            while (true) {
                final IElementType tt = token();
                if (tt == null || tt == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER || tt == XmlTokenType.XML_END_TAG_START || tt == XmlTokenType
                    .XML_EMPTY_ELEMENT_END ||
                    tt == XmlTokenType.XML_START_TAG_START) {
                    break;
                }

                if (tt == XmlTokenType.XML_BAD_CHARACTER) {
                    final PsiBuilder.Marker error = mark();
                    advance();
                    error.error(XmlErrorBundle.message("unescaped.ampersand.or.nonterminated.character.entity.reference"));
                } else if (tt == XmlTokenType.XML_ENTITY_REF_TOKEN) {
                    parseReference();
                } else {
                    advance();
                }
            }

            if (token() == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER) {
                advance();
            } else {
                error(XmlErrorBundle.message("xml.parsing.unclosed.attribute.value"));
            }
        } else {
            if (token() != XmlTokenType.XML_TAG_END && token() != XmlTokenType.XML_EMPTY_ELEMENT_END) {
                advance(); // Single token att value
            }
        }

        attValue.done(XmlElementType.XML_ATTRIBUTE_VALUE);
    }

    private void parseProlog() {
        while (true) {
            final IElementType tt = token();
            if (tt == XmlTokenType.XML_COMMENT_START) {
                parseComment();
            } else if (tt == XmlTokenType.XML_REAL_WHITE_SPACE) {
                advance();
            } else {
                break;
            }
        }

        final PsiBuilder.Marker prolog = mark();
        while (true) {
            final IElementType tt = token();
            if (tt == XmlTokenType.XML_PI_START) {
                parseProcessingInstruction();
            } else if (tt == XmlTokenType.XML_DOCTYPE_START) {
                parseDoctype();
            } else if (tt == XmlTokenType.XML_COMMENT_START) {
                parseComment();
            } else if (tt == XmlTokenType.XML_REAL_WHITE_SPACE) {
                advance();
            } else {
                break;
            }
        }
        prolog.done(XmlElementType.XML_PROLOG);
    }

    protected void parseProcessingInstruction() {
        assert token() == XmlTokenType.XML_PI_START;
        final PsiBuilder.Marker pi = mark();
        advance();
        if (token() == XmlTokenType.XML_NAME || token() == XmlTokenType.XML_PI_TARGET) {
            advance();
        }

        while (token() == XmlTokenType.XML_NAME) {
            advance();
            if (token() == XmlTokenType.XML_EQ) {
                advance();
            } else {
                error(XmlErrorBundle.message("expected.attribute.eq.sign"));
            }
            parseAttributeValue();
        }

        if (token() == XmlTokenType.XML_PI_END) {
            advance();
        } else {
            error(XmlErrorBundle.message("xml.parsing.unterminated.processing.instruction"));
        }

        pi.done(XmlElementType.XML_PROCESSING_INSTRUCTION);
    }

    protected void error(@NotNull String message) {
        myBuilder.error(message);
    }
}

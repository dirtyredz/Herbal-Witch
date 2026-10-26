package io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation;

import guideme.compiler.PageCompiler;
import guideme.compiler.TagCompiler;
import guideme.compiler.tags.MdxAttrs;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytParagraph;
import guideme.document.flow.LytFlowLink;
import guideme.document.flow.LytFlowText;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import net.minecraft.client.resources.language.I18n;

import java.util.Set;

/**
 * {@code <HerbalWatch lesson="herbalwitch:first_preparation" />}: a one-line link that opens the animated
 * lesson. Emits nothing when no animation player is installed (Ponder absent); the page's written steps stay
 * complete either way.
 */
public final class WatchLessonTag implements TagCompiler {
    public static final String TAG = "HerbalWatch";
    public static final String LESSON_ATTRIBUTE = "lesson";

    @Override
    public Set<String> getTagNames() {
        return Set.of(TAG);
    }

    @Override
    public void compileBlockContext(PageCompiler compiler, LytBlockContainer parent, MdxJsxFlowElement element) {
        String id = MdxAttrs.getString(compiler, parent, element, LESSON_ATTRIBUTE, null);
        var problem = GuideAnimations.problem(id);
        if (problem.isPresent()) {
            parent.appendError(compiler, problem.get(), element);
            return;
        }
        var lesson = GuideLessons.find(id).orElseThrow();
        if (!GuideAnimations.available(lesson)) return;
        var link = new LytFlowLink();
        link.append(LytFlowText.of(I18n.get(GuideLessons.WATCH_LABEL_KEY)));
        link.setClickCallback(host -> GuideAnimations.open(lesson.id()));
        var paragraph = new LytParagraph();
        paragraph.append(link);
        parent.append(paragraph);
    }
}

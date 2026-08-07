package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategoryRelation;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveCategoryTreeVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiveCategoryTreeBuilderTest {

    @Test
    void buildsOrderedTreeAndCopiesCategoriesWithMultipleParents() {
        List<LiveCategoryTreeVO> categories = List.of(
                category(1, "root-one"),
                category(2, "root-two"),
                category(3, "child"),
                category(4, "shared")
        );
        List<LiveCategoryRelation> relations = List.of(
                relation(1, 3),
                relation(2, 4),
                relation(1, 4)
        );

        List<LiveCategoryTreeVO> tree = LiveCategoryTreeBuilder.build(categories, relations);

        assertEquals(List.of(1, 2), tree.stream().map(LiveCategoryTreeVO::getId).toList());
        assertEquals(List.of(3, 4), tree.get(0).getChildren().stream().map(LiveCategoryTreeVO::getId).toList());
        assertEquals(List.of(4), tree.get(1).getChildren().stream().map(LiveCategoryTreeVO::getId).toList());
        assertEquals(1, tree.get(0).getChildren().get(1).getParentId());
        assertEquals(2, tree.get(1).getChildren().get(0).getParentId());
        assertNotSame(tree.get(0).getChildren().get(1), tree.get(1).getChildren().get(0));
    }

    @Test
    void acceptsExplicitRootRelationsAndStopsCycles() {
        List<LiveCategoryTreeVO> tree = LiveCategoryTreeBuilder.build(
                List.of(category(1, "root")),
                List.of(relation(0, 1), relation(1, 1))
        );

        assertEquals(1, tree.size());
        assertEquals(0, tree.get(0).getParentId());
        assertNull(tree.get(0).getChildren());
    }

    @Test
    void buildsImplicitRootsAndPreservesResponseFields() {
        LiveCategoryTreeVO category = category(1, "root");
        category.setIconUrl("https://example.com/icon.png");

        List<LiveCategoryTreeVO> tree = LiveCategoryTreeBuilder.build(List.of(category), null);

        assertEquals(1, tree.size());
        assertEquals(0, tree.get(0).getParentId());
        assertEquals("root", tree.get(0).getName());
        assertEquals("root", tree.get(0).getValue());
        assertEquals("https://example.com/icon.png", tree.get(0).getIconUrl());
        assertNull(tree.get(0).getChildren());
    }

    @Test
    void ignoresDuplicateAndIncompleteRelationsAndKeepsOrphansUnreachable() {
        LiveCategoryRelation missingCategory = relation(1, 99);
        missingCategory.setCategoryId(null);
        LiveCategoryRelation missingParent = relation(1, 99);
        missingParent.setParentId(null);

        List<LiveCategoryTreeVO> tree = LiveCategoryTreeBuilder.build(
                List.of(category(1, "root"), category(2, "child"), category(3, "orphan")),
                List.of(
                        relation(1, 2),
                        relation(1, 2),
                        relation(99, 3),
                        relation(1, 99),
                        missingCategory,
                        missingParent
                )
        );

        assertEquals(List.of(1), tree.stream().map(LiveCategoryTreeVO::getId).toList());
        assertEquals(List.of(2), tree.get(0).getChildren().stream().map(LiveCategoryTreeVO::getId).toList());
    }

    @Test
    void stopsMultiNodeCyclesAtTheCurrentPath() {
        List<LiveCategoryTreeVO> tree = LiveCategoryTreeBuilder.build(
                List.of(category(1, "root"), category(2, "child"), category(3, "grandchild")),
                List.of(relation(1, 2), relation(2, 3), relation(3, 2))
        );

        LiveCategoryTreeVO child = tree.get(0).getChildren().get(0);
        LiveCategoryTreeVO grandchild = child.getChildren().get(0);
        assertEquals(2, child.getId());
        assertEquals(3, grandchild.getId());
        assertNull(grandchild.getChildren());
    }

    @Test
    void returnsEmptyTreeWithoutCategories() {
        assertTrue(LiveCategoryTreeBuilder.build(List.of(), List.of()).isEmpty());
        assertTrue(LiveCategoryTreeBuilder.build(null, List.of()).isEmpty());
    }

    private static LiveCategoryTreeVO category(int id, String name) {
        LiveCategoryTreeVO category = new LiveCategoryTreeVO();
        category.setId(id);
        category.setName(name);
        category.setValue(name);
        return category;
    }

    private static LiveCategoryRelation relation(int parentId, int categoryId) {
        LiveCategoryRelation relation = new LiveCategoryRelation();
        relation.setParentId(parentId);
        relation.setCategoryId(categoryId);
        return relation;
    }
}

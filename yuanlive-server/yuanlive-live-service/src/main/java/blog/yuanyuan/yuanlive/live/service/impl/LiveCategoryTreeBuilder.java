package blog.yuanyuan.yuanlive.live.service.impl;

import blog.yuanyuan.yuanlive.entity.live.entity.LiveCategoryRelation;
import blog.yuanyuan.yuanlive.live.domain.vo.LiveCategoryTreeVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class LiveCategoryTreeBuilder {

    private static final int ROOT_PARENT_ID = 0;

    private LiveCategoryTreeBuilder() {
    }

    static List<LiveCategoryTreeVO> build(
            List<LiveCategoryTreeVO> categories,
            List<LiveCategoryRelation> relations
    ) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        Map<Integer, Set<Integer>> parentIdsByCategory = indexParentIds(relations);
        Map<Integer, List<LiveCategoryTreeVO>> categoriesByParent = new HashMap<>();

        for (LiveCategoryTreeVO category : categories) {
            Set<Integer> parentIds = parentIdsByCategory.get(category.getId());
            if (parentIds == null || parentIds.isEmpty()) {
                categoriesByParent.computeIfAbsent(ROOT_PARENT_ID, ignored -> new ArrayList<>()).add(category);
                continue;
            }
            for (Integer parentId : parentIds) {
                categoriesByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(category);
            }
        }

        return buildLevel(categoriesByParent, ROOT_PARENT_ID, new HashSet<>());
    }

    private static Map<Integer, Set<Integer>> indexParentIds(List<LiveCategoryRelation> relations) {
        Map<Integer, Set<Integer>> parentIdsByCategory = new HashMap<>();
        if (relations == null) {
            return parentIdsByCategory;
        }

        for (LiveCategoryRelation relation : relations) {
            if (relation.getCategoryId() == null || relation.getParentId() == null) {
                continue;
            }
            parentIdsByCategory
                    .computeIfAbsent(relation.getCategoryId(), ignored -> new LinkedHashSet<>())
                    .add(relation.getParentId());
        }
        return parentIdsByCategory;
    }

    private static List<LiveCategoryTreeVO> buildLevel(
            Map<Integer, List<LiveCategoryTreeVO>> categoriesByParent,
            Integer parentId,
            Set<Integer> path
    ) {
        List<LiveCategoryTreeVO> tree = new ArrayList<>();
        for (LiveCategoryTreeVO category : categoriesByParent.getOrDefault(parentId, List.of())) {
            if (!path.add(category.getId())) {
                continue;
            }
            try {
                LiveCategoryTreeVO node = copyNode(category, parentId);
                List<LiveCategoryTreeVO> children = buildLevel(categoriesByParent, category.getId(), path);
                if (!children.isEmpty()) {
                    node.setChildren(children);
                }
                tree.add(node);
            } finally {
                path.remove(category.getId());
            }
        }
        return tree;
    }

    private static LiveCategoryTreeVO copyNode(LiveCategoryTreeVO category, Integer parentId) {
        LiveCategoryTreeVO node = new LiveCategoryTreeVO();
        node.setId(category.getId());
        node.setName(category.getName());
        node.setIconUrl(category.getIconUrl());
        node.setValue(category.getValue());
        node.setParentId(parentId);
        return node;
    }
}

package com.study.ecommerce.domain.category.service;

import com.study.ecommerce.domain.category.dto.req.CategoryRequest;
import com.study.ecommerce.domain.category.dto.resp.CategoryResponse;
import com.study.ecommerce.domain.category.entity.Category;
import com.study.ecommerce.domain.category.repository.CategoryRepository;
import com.study.ecommerce.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceCustom implements CategoryService{
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        //모든 카테고리 조회
        List<Category> allCategories = categoryRepository.findAll() ;

        // 부모 Id로 카테고리 그룹화
        Map<Long,List<Category>> childenMap = allCategories.stream()
                //최상위 카테고리는 제외
                .filter(cat -> cat.getParentId() != null)
                //부모 Id로 그룹화
                .collect(Collectors.groupingBy(Category::getParentId));

        //최상위 카테고리만 가져옴
        List<Category> rooCategories = allCategories.stream()
                .filter(cat -> cat.getParentId() == null)
                .toList();

      return rooCategories.stream()
                .map(cat -> buildCategoryResponse(cat,childenMap))
              .toList();

    }

    private CategoryResponse buildCategoryResponse(Category category,Map<Long,List<Category>> childrenMap){
        List<CategoryResponse> children = new ArrayList<>();
        //자식 카테고리가 존재하면
        if(childrenMap.containsKey(category.getId())){
            //자식 카테고리를 조회해서 CategoryResponse로 변환
            children = childrenMap.get(category.getId()).stream()
                    //자식 카테고리가 존재하면
                    .map(child -> buildCategoryResponse(child,childrenMap))
                    .toList();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDepth(),
                category.getParentId(),
                children
        );
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
//        List<Category> allCategories = categoryRepository.findAll();
//
//        Map<Long,List<Category>> childenMap = allCategories.stream()
//                .filter(cat -> cat.getParentId() != null)
//                .collect(Collectors.groupingBy(Category::getParentId));
//
//        Category rootCategory = allCategories.stream()
//                .filter(cat -> cat.getId().equals(id))
//                .findFirst()
//                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카테고리입니다"));
//
//        return buildCategoryResponse(rootCategory,childenMap);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리 찾을수 없음"));

        List<Category> allCategories = categoryRepository.findAll();

        Map<Long,List<Category>> childenMap = allCategories.stream()
                .filter(cat -> cat.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        return null;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Long parentId = null;

        //최상단 카테고리
        int depth = 1;

        //이조건에 들지 않으면 최상위 카테고리
        if(request.parentId() != null){
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("상위 카테고리 찾을수 없습니다"));

            parentId = parent.getId();

            //부모 카테고리의 깊이 + 1
            depth = parent.getDepth() + 1;

        }

        Category category = Category.builder()
                .name(request.name())
                .depth(depth)
                .parentId(parentId)
                .build();
        categoryRepository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDepth(),
                category.getParentId(),
                List.of()
        );
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을수 없습니다"));

        Long parentId = null;
        int depth = 1;

       if(request.parentId() == null){
           Category parent  = categoryRepository.findById(request.parentId())
                   .orElseThrow(() -> new EntityNotFoundException("상위 카테고리를 찾을수 없습니다"));

           parentId = parent.getId();
           depth = parent.getDepth()+1;

           //자기 자신을 부모로 설정하는 경우 방지
           if(parent.getId().equals(category.getId())){
               throw new IllegalArgumentException("자기 자신을 상위 카테고리롤 설정 못함");
           }

           // 자신이 하위 카테고리르 부모로 설정하는 순환참조 금지
           if(isDescendant(category, parent)){
               throw new IllegalArgumentException("하위 카테고리를 상위카테고리롤 설정 못함");
           }
       }

        Category updateCategory = Category.builder()
                .name(request.name())
                .parentId(request.parentId())
                .depth(depth)
                .build();

        Category updated =  categoryRepository.save(updateCategory);

        return new CategoryResponse(updated.getId()
                ,updated.getName(),updated.getDepth(),
                updated.getParentId(),List.of()
        );
    }

    @Override
    public void deleteCategoryById(Long id) {
//        categoryRepository.findById(id)
//                        .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을수 없습니다"));
//
//        List<Category> children = categoryRepository.findByParentId(id);
//        categoryRepository.deleteById(id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을수 없습니다"));
        List<Category> children = categoryRepository.findByParentId(id);

        if (!children.isEmpty()) {
            throw new IllegalArgumentException("하위 카테고리가 있는 카테고리는 삭제할 수 없습니다.");
        }

        Long productCount = categoryRepository.countProductsByCategory(id);
        if (productCount > 0){
            throw new IllegalArgumentException("카테고리에 속한 상품이 있는 경우 삭젤할 수 없습니다");
        }
        categoryRepository.delete(category);
    }

    //하위 카테고리 유무 체크
    private boolean isDescendant(Category ancestor, Category descendant){
        if(descendant.getParentId() == null){
            return false;
        }

        if (descendant.getParentId().equals(ancestor.getId())){
            return true;
        }

        //descendant의 부모 카테고리를 조회
        Category parent =  categoryRepository.findById(descendant.getParentId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을수 없습니다"));

        return isDescendant(ancestor, parent);
    }
}

package hello.springcommunity.common.validation;

import hello.springcommunity.domain.post.CategoryCode;

public class SortAndCategoryValidator {

    public static CategoryCode checkCategory(String category) {

        CategoryCode[] values = CategoryCode.values();
        CategoryCode categoryCode = null;

        if(category != null) {
            for(CategoryCode code : values) {
                if(code.name().equals(category)) categoryCode = code;
            }
        }

        return categoryCode;
    }


    public static String checkSort(String sort) {
        //id, viewCount, likeCount

        if(!sort.equals("id")) {

            if(!sort.equals("viewCount") && !sort.equals("likeCount")) {
                sort = "id";
            }
        }

        return sort;
    }
}

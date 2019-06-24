package com.nbcsports.regional.nbc_rsn.common;

import com.nbcsports.regional.nbc_rsn.editorial_detail.models.EditorialDetailItem;

import java.util.List;

public class EditorialDetailsFeed {

    EditorialDetail editorialDetail;

    public EditorialDetail getEditorialDetail() {
        return editorialDetail;
    }

    public static class EditorialDetail {

        List<EditorialDetailItem> components;

        public List<EditorialDetailItem> getComponents() {
            return components;
        }
    }
}

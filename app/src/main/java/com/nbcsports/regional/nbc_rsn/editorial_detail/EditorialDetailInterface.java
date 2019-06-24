package com.nbcsports.regional.nbc_rsn.editorial_detail;

/**
 * Created by arkady on 2018-05-22.
 */

public interface EditorialDetailInterface {
    void closePage(boolean animated);

    void closeAllPages(boolean animated);

    boolean hasAtLeastOneStackableViewOpened();
}

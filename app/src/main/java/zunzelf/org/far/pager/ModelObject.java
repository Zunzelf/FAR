package zunzelf.org.far.pager;


import zunzelf.org.far.R;

public enum ModelObject {

    RED(R.string.red, R.layout.home),
    BLUE(R.string.blue, R.layout.page2),
    GREEN(R.string.green, R.layout.page3);

    private int mTitleResId;
    private int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}

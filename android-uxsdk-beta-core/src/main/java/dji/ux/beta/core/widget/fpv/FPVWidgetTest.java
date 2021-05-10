package dji.ux.beta.core.widget.fpv;

import android.content.Context;
import android.util.AttributeSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dji.ux.beta.core.base.widget.ConstraintLayoutWidget;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2021年05月06日19:30
 * @Email: 971613168@qq.com
 */
public class FPVWidgetTest extends ConstraintLayoutWidget {
    public FPVWidgetTest(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

    }

    @Override
    protected void reactToModelChanges() {

    }

    @Nullable
    @Override
    public String getIdealDimensionRatioString() {
        return null;
    }
}

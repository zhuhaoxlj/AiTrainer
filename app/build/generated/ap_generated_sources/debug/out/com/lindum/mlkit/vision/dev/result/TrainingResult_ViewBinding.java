// Generated code from Butter Knife. Do not modify!
package com.lindum.mlkit.vision.dev.result;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.lindum.mlkit.vision.dev.R;
import com.xuexiang.xui.widget.progress.CircleProgressView;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class TrainingResult_ViewBinding implements Unbinder {
  private TrainingResult target;

  @UiThread
  public TrainingResult_ViewBinding(TrainingResult target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public TrainingResult_ViewBinding(TrainingResult target, View source) {
    this.target = target;

    target.progressViewCircleMain = Utils.findRequiredViewAsType(source, R.id.progressView_circle_main, "field 'progressViewCircleMain'", CircleProgressView.class);
    target.progressTextMain = Utils.findRequiredViewAsType(source, R.id.progress_text_main, "field 'progressTextMain'", TextView.class);
    target.hpvTime = Utils.findRequiredViewAsType(source, R.id.hpv_time, "field 'hpvTime'", HorizontalProgressView.class);
    target.progressTextTime = Utils.findRequiredViewAsType(source, R.id.progress_text_time, "field 'progressTextTime'", TextView.class);
    target.hpvCal = Utils.findRequiredViewAsType(source, R.id.hpv_cal, "field 'hpvCal'", HorizontalProgressView.class);
    target.progressTextCal = Utils.findRequiredViewAsType(source, R.id.progress_text_cal, "field 'progressTextCal'", TextView.class);
    target.hpvGroup = Utils.findRequiredViewAsType(source, R.id.hpv_group, "field 'hpvGroup'", HorizontalProgressView.class);
    target.progressTextGroup = Utils.findRequiredViewAsType(source, R.id.progress_text_group, "field 'progressTextGroup'", TextView.class);
    target.hpvWrong = Utils.findRequiredViewAsType(source, R.id.hpv_wrong, "field 'hpvWrong'", HorizontalProgressView.class);
    target.progressTextWrong = Utils.findRequiredViewAsType(source, R.id.progress_text_wrong, "field 'progressTextWrong'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    TrainingResult target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.progressViewCircleMain = null;
    target.progressTextMain = null;
    target.hpvTime = null;
    target.progressTextTime = null;
    target.hpvCal = null;
    target.progressTextCal = null;
    target.hpvGroup = null;
    target.progressTextGroup = null;
    target.hpvWrong = null;
    target.progressTextWrong = null;
  }
}

package nxp.activentag5i2c.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SuccessDialogUtil {

    /**
     * Shows a beautiful success dialog that auto-dismisses after 2 seconds
     * @param context Activity context
     * @param title Title text (e.g., "New Settings Updated successfully")
     * @param duration Duration in milliseconds (default 2000)
     */
    public static void showSuccessDialog(Context context, String title, long duration) {
        // Create dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(createDialogView(context, title));

        // Make background transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set dialog size and position
        dialog.getWindow().setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        dialog.getWindow().setGravity(Gravity.CENTER);

        // Show dialog
        dialog.show();

        // Auto-dismiss after specified duration
        dialog.getWindow().getDecorView().postDelayed(dialog::dismiss, duration);
    }

    /**
     * Overloaded method with default 2-second duration
     */
    public static void showSuccessDialog(Context context, String title) {
        showSuccessDialog(context, title, 2000);
    }

    private static View createDialogView(Context context, String title) {
        // Main container - fills entire screen and centers content
        LinearLayout mainContainer = new LinearLayout(context);
        mainContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mainContainer.setGravity(Gravity.CENTER);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        // Dialog card (white background with rounded appearance)
        LinearLayout dialogCard = new LinearLayout(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                dpToPx(context, 280),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.gravity = Gravity.CENTER;
        dialogCard.setLayoutParams(cardParams);
        dialogCard.setOrientation(LinearLayout.VERTICAL);
        dialogCard.setGravity(Gravity.CENTER);
        dialogCard.setPadding(dpToPx(context, 30), dpToPx(context, 40), dpToPx(context, 30), dpToPx(context, 40));
        dialogCard.setBackgroundColor(Color.WHITE);

        // Add shadow and rounded corners using custom background
        dialogCard.setBackground(createRoundedBackground(context));

        // Success checkmark icon
        ImageView checkIcon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(context, 70),
                dpToPx(context, 70)
        );
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        iconParams.bottomMargin = dpToPx(context, 16);
        checkIcon.setLayoutParams(iconParams);

        // Create checkmark drawable programmatically
        checkIcon.setImageDrawable(createCheckmarkDrawable(context));
        checkIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        dialogCard.addView(checkIcon);

        // Success text - PERFECTLY CENTERED
        TextView titleText = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.CENTER;
        titleText.setLayoutParams(textParams);
        titleText.setTextSize(16);
        titleText.setTextColor(Color.parseColor("#1E3F82"));
        titleText.setTypeface(titleText.getTypeface(), android.graphics.Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER); // Center align text inside TextView
        titleText.setMaxLines(3);
        titleText.setLineSpacing(0, 1.2f); // Adjust line spacing for better centering
        titleText.setText(title); // Use the passed title directly
        dialogCard.addView(titleText);

        mainContainer.addView(dialogCard);
        return mainContainer;
    }

    private static android.graphics.drawable.Drawable createRoundedBackground(Context context) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(dpToPx(context, 20));
        return drawable;
    }

    private static android.graphics.drawable.Drawable createCheckmarkDrawable(Context context) {
        // Create a circular green background with checkmark
        android.graphics.drawable.ShapeDrawable shapeDrawable = new android.graphics.drawable.ShapeDrawable(
                new android.graphics.drawable.shapes.OvalShape()
        );
        shapeDrawable.getPaint().setColor(Color.parseColor("#4CAF50"));

        return shapeDrawable;
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
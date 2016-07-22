package imagemanipulations.zome.android.com;

/**
 * Created by Sasha on 6/30/2016.
 */
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the  {@link
 * ScreenSlideActivity} samples.</p>
 */
public class ScreenSlidePageFragment extends Fragment {
	/**
	 * The argument key for the page number this fragment represents.
	 */
	public static final String ARG_PAGE = "page";

	/**
	 * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
	 */
	private int mPageNumber;

	/**
	 * Factory method for this fragment class. Constructs a new fragment for the given page number.
	 */
	public static ScreenSlidePageFragment create(int pageNumber) {
		ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, pageNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public ScreenSlidePageFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPageNumber = getArguments().getInt(ARG_PAGE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout containing a title and body text.
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

		// Set the title view to show the page number.
		((TextView) rootView.findViewById(R.id.help_title)).setText(getString(R.string.title_template_step, mPageNumber + 1));
		String[] texts = getResources().getStringArray(R.array.help_texts);
		int id;
		switch(mPageNumber) {
			case 0:
				id = R.drawable.help_img_00;
				break;
			case 1:
				id = R.drawable.help_img_01;
				break;
			case 2:
				id = R.drawable.help_img_02;
				break;
			case 3:
				id = R.drawable.help_img_03;
				break;
			default:
				id = R.drawable.view_image_icon;
		}
		((ImageView) rootView.findViewById(R.id.help_image)).setImageResource(id);
		((TextView) rootView.findViewById(R.id.help_text)).setText(texts.length > mPageNumber?texts[mPageNumber]:"");

		return rootView;
	}

	/**
	 * Returns the page number represented by this fragment object.
	 */
	public int getPageNumber() {
		return mPageNumber;
	}
}


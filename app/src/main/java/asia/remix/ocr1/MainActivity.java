package asia.remix.ocr1;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InputStream;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class MainActivity extends AppCompatActivity{
	static final String TAG = MainActivity.class.getSimpleName();

	Bitmap getBitmapFromAsset( String strName ){
		AssetManager assetManager = getAssets();
		InputStream inputStream = null;
		try{
			inputStream = assetManager.open( strName );
		}catch( IOException e ){
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream( inputStream );
		return bitmap;
	}

	OnFailureListener onFailureListener = new OnFailureListener(){
		@Override
		public void onFailure( @NonNull Exception e ){
			// Task failed with an exception
			Log.w( TAG, "●OnFailureListener()" );
		}
	};

	OnSuccessListener onSuccessListener = new OnSuccessListener<FirebaseVisionText>(){
		@Override
		public void onSuccess( FirebaseVisionText result ){
			// Task completed successfully
			Log.d( TAG, "●onSuccess()" + result.getText() );

			overlayView.clear();
			for( FirebaseVisionText.TextBlock block : result.getTextBlocks() ){
				String blockText = block.getText();
				Log.d( TAG, String.format( "●[%s]", blockText ) );
				for( FirebaseVisionText.Line line : block.getLines() ){
					for( FirebaseVisionText.Element element : line.getElements() ){
						overlayView.add( new TextGraphic( overlayView, element ) );
					}
				}
			}

		}
	};

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		FloatingActionButton fab = findViewById( R.id.fab );
		fab.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick( View view ){
			}
		} );
	}

	OverlayView overlayView;
	@Override
	public void onWindowFocusChanged( boolean hasFocus ){
		super.onWindowFocusChanged( hasFocus );
		Log.d( TAG, "●onWindowFocusChanged()" );

		Bitmap bitmap = getBitmapFromAsset( "sample.jpg" );
		ImageView imageView = (ImageView)findViewById( R.id.image_view );
		float fScaleW = (float)bitmap.getWidth() / imageView.getWidth();
		Log.d( TAG, String.format( "W %d / %d = %.5f", bitmap.getWidth(),  imageView.getWidth(), fScaleW ) );
		float fScaleH = (float)bitmap.getHeight() / imageView.getHeight();
		Log.d( TAG, String.format( "H %d / %d = %.5f", bitmap.getHeight(),  imageView.getHeight(), fScaleH ) );
		overlayView = findViewById( R.id.overlay );
		//左上を原点に対象画像を画面に収めた時の縦横どちらかの隙間分を加算する
		if( fScaleW > fScaleH ){//縮小しないほうが正しくデジタル数値認識
			overlayView.setImageInfo( bitmap.getWidth(), (int)(imageView.getHeight()*fScaleW) );
		}else{
			overlayView.setImageInfo( (int)(imageView.getWidth()*fScaleH), bitmap.getHeight() );
		}
		imageView.setImageBitmap( bitmap );
		FirebaseVisionImage image = FirebaseVisionImage.fromBitmap( bitmap );
		FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
		Task task = recognizer.processImage( image );
		task.addOnSuccessListener( onSuccessListener );
		task.addOnFailureListener( onFailureListener );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ){
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if( id == R.id.action_settings ){
			return true;
		}

		return super.onOptionsItemSelected( item );
	}
}
package asia.remix.ocr1;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
			Log.w( TAG, "●OnFailureListener()" );
		}
	};

	OnSuccessListener onSuccessListener = new OnSuccessListener<FirebaseVisionText>(){
		@Override
		public void onSuccess( FirebaseVisionText result ){
			Log.d( TAG, "●onSuccess()" + result.getText() );
			for( FirebaseVisionText.TextBlock block : result.getTextBlocks() ){
				String blockText = block.getText();
				Log.d( TAG, String.format( "●[%s]", blockText ) );
			}
		}
	};

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		Toolbar toolbar = findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		Bitmap bitmap = getBitmapFromAsset( "sample.jpg" );
		FirebaseVisionImage image = FirebaseVisionImage.fromBitmap( bitmap );
		FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
		Task task = recognizer.processImage( image );
		task.addOnSuccessListener( onSuccessListener );
		task.addOnFailureListener( onFailureListener );

		FloatingActionButton fab = findViewById( R.id.fab );
		fab.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick( View view ){
				Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
			}
		} );
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
package com.example.myapplication.reactlibrary;

import static com.reactlibrary.constants.IVoiceChangerConstants.NAME_FOLDER_RECORD;
import static com.un4seen.bass.BASS.BASS_CONFIG_FLOAT;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.reactlibrary.basseffect.DBMediaPlayer;
import com.reactlibrary.basseffect.IDBMediaListener;
import com.reactlibrary.dataMng.JsonParsingUtils;
import com.reactlibrary.object.EffectObject;
import com.reactlibrary.task.DBTask;
import com.reactlibrary.task.IDBCallback;
import com.reactlibrary.task.IDBTaskListener;
import com.reactlibrary.utils.ApplicationUtils;
import com.reactlibrary.utils.DBLog;
import com.reactlibrary.utils.ISaveEffectCallback;
import com.reactlibrary.utils.StringUtils;
import com.un4seen.bass.BASS;

import java.io.File;
import java.util.ArrayList;

public class VoiceChangerModule {
    public static final String TAG = VoiceChangerModule.class.getSimpleName();
    public ArrayList<EffectObject> effectObjects;
    private String mPathAudio;
    private DBMediaPlayer mDBMedia;
    private boolean isInit;
    private Integer playingIndex;
    private File outputDir;
    private String mNameExportVoice;
    private Context context;

    public VoiceChangerModule(Context context) {
        this.effectObjects = new ArrayList<>();
        this.mPathAudio = null;
        this.playingIndex = null;
        this.outputDir = null;
        this.context = context;
        setDBMedia(this.mDBMedia);
        this.onInitAudioDevice();
    }

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";

    public Integer getPlayingIndex() {
        return playingIndex;
    }

    public void insertEffect(String effect) {
        this.effectObjects.add(JsonParsingUtils.jsonToEffectObject(effect));
    }

    public DBMediaPlayer getDBMedia() {
        return mDBMedia;
    }

    public void setDBMedia(DBMediaPlayer mDBMedia) {
        this.mDBMedia = mDBMedia;
    }

    public void show(String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public void setPath(String path) {
        this.mPathAudio = path;
    }

    public void setPlayingIndex(Integer idx) {
        if (idx != null) {
            this.playingIndex = idx;
            //WritableMap params = Arguments.createMap();
            //params.putInt("index", idx);
            //sendEvent(reactContext, "idxMediaPlaying", params);
        }
    }

    public void saveEffect(int effectIndex, String effectName, ISaveEffectCallback callback) {
        onSaveEffect(this.effectObjects.get(effectIndex), effectName, callback);
    }

    public void createOutputDir() {
        this.outputDir = this.getDir();
    }

    public void createDBMedia() {
        this.onCreateDBMedia();
    }

    public void playEffect(int effectIndex, PlayCallback playCallback) {
        try {
            Log.d(TAG, "audioPath: " + this.mPathAudio);

            if (!StringUtils.isEmptyString(mPathAudio)) {
                File mFile = new File(mPathAudio);
                if (!(mFile.exists() && mFile.isFile())) {
                    playCallback.onFail();
                }
            }

            try {
                this.setPlayingIndex(effectIndex);
                Log.e(TAG, "playEffect: " + effectObjects.get(effectIndex).getName());
                onPlayEffect(this.effectObjects.get(effectIndex));
                // promise.resolve(true);
            } catch (Exception ex) {
                // promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void onPlayEffect(EffectObject mEffectObject) {
        boolean isPlaying = mEffectObject.isPlaying();
        if (isPlaying) {
            mEffectObject.setPlaying(false);
            if (mDBMedia != null) {
                mDBMedia.pauseAudio();
            }
            //sendEvent(reactContext, "onMediaCompletion", null);
        } else {
            onResetState();
            mEffectObject.setPlaying(true);
            if (mDBMedia != null) {
                mDBMedia.setPathMix(mEffectObject.getPathMix());
                mDBMedia.setNeedMix(mEffectObject.isMix());
                mDBMedia.prepareAudio();

                mDBMedia.setReverse(mEffectObject.isReverse());
                mDBMedia.setAudioPitch(mEffectObject.getPitch());
                mDBMedia.setCompressor(mEffectObject.getCompressor());
                mDBMedia.setAudioRate(mEffectObject.getRate());
                mDBMedia.setAudioEQ1(mEffectObject.getEq1());
                mDBMedia.setAudioEQ2(mEffectObject.getEq2());
                mDBMedia.setAudioEQ3(mEffectObject.getEq3());
                mDBMedia.setPhaser(mEffectObject.getPhaser());
                mDBMedia.setAutoWah(mEffectObject.getAutoWah());
                mDBMedia.setAudioReverb(mEffectObject.getReverb());
                mDBMedia.setEcho4Effect(mEffectObject.getEcho4());
                mDBMedia.setAudioEcho(mEffectObject.getEcho());
                mDBMedia.setBiQuadFilter(mEffectObject.getFilter());
                mDBMedia.setFlangeEffect(mEffectObject.getFlange());
                mDBMedia.setChorus(mEffectObject.getChorus());
                mDBMedia.setAmplify(mEffectObject.getAmplify());
                mDBMedia.setDistort(mEffectObject.getDistort());
                mDBMedia.setRotate(mEffectObject.getRotate());

                mDBMedia.startAudio();
            }
        }
    }

    private void onInitAudioDevice() {
        if (!isInit) {
            isInit = true;
            if (!BASS.BASS_Init(-1, 44100, 0)) {
                new Exception(TAG + " Can't initialize device").printStackTrace();
                this.isInit = false;
                return;
            }
            String libpath = context.getApplicationInfo().nativeLibraryDir;
            try {
                BASS.BASS_PluginLoad(libpath + "/libbass_fx.so", 0);
                BASS.BASS_PluginLoad(libpath + "/libbassenc.so", 0);
                BASS.BASS_PluginLoad(libpath + "/libbassmix.so", 0);
                BASS.BASS_PluginLoad(libpath + "/libbasswv.so", 0);
                int floatsupport = BASS.BASS_GetConfig(BASS_CONFIG_FLOAT);
                DBLog.d(TAG, "=======>floatsupport=" + floatsupport);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onSaveEffect(final EffectObject mEffectObject, String effectName, ISaveEffectCallback callback) {
        callback.onSaving();
        if (mDBMedia != null) {
            //onResetState();
        }
        if (mDBMedia != null) {
            mNameExportVoice = effectName + ".wav";
            startSaveEffect(mEffectObject, new IDBCallback() {
                @Override
                public void onAction() {
                    final File mOutPutFile = new File(outputDir, effectName);
                    if (mOutPutFile.exists()) {
                        String mInfoSave = String.format("Your voice path is %1$s", mOutPutFile.getAbsolutePath());
                        showToast(mInfoSave);
                        //promise.resolve(mOutPutFile.getAbsolutePath());
                    }
                    callback.onSuccess(mOutPutFile.getAbsolutePath());
                }
            });
        }
    }

    private void startSaveEffect(final EffectObject mEffectObject, final IDBCallback mDBCallback) {
        final File mTempOutPutFile = new File(outputDir, mNameExportVoice);

        final DBMediaPlayer mDBExportMedia = new DBMediaPlayer(mPathAudio);
//        mDBExportMedia.setPathMix(mEffectObject.getPathMix());
//        mDBExportMedia.setNeedMix(mEffectObject.isMix());

        DBTask mDBTask = new DBTask(new IDBTaskListener() {

            @Override
            public void onPreExecute() {

            }

            @Override
            public void onDoInBackground() {
                boolean b = mDBExportMedia.initMediaToSave();
                if (b) {
                    mDBExportMedia.setReverse(mEffectObject.isReverse());
                    mDBExportMedia.setAudioPitch(mEffectObject.getPitch());
                    mDBExportMedia.setCompressor(mEffectObject.getCompressor());
                    mDBExportMedia.setAudioRate(mEffectObject.getRate());
                    mDBExportMedia.setAudioEQ1(mEffectObject.getEq1());
                    mDBExportMedia.setAudioEQ2(mEffectObject.getEq2());
                    mDBExportMedia.setAudioEQ3(mEffectObject.getEq3());
                    mDBExportMedia.setPhaser(mEffectObject.getPhaser());
                    mDBExportMedia.setAutoWah(mEffectObject.getAutoWah());
                    mDBExportMedia.setAudioReverb(mEffectObject.getReverb());
                    mDBExportMedia.setEcho4Effect(mEffectObject.getEcho4());
                    mDBExportMedia.setAudioEcho(mEffectObject.getEcho());

                    mDBExportMedia.setBiQuadFilter(mEffectObject.getFilter());
                    mDBExportMedia.setFlangeEffect(mEffectObject.getFlange());
                    mDBExportMedia.setChorus(mEffectObject.getChorus());
                    mDBExportMedia.setAmplify(mEffectObject.getAmplify());
                    mDBExportMedia.setDistort(mEffectObject.getDistort());
                    mDBExportMedia.setRotate(mEffectObject.getRotate());

                    mDBExportMedia.saveToFile(mTempOutPutFile.getAbsolutePath());
                    mDBExportMedia.releaseAudio();
                }
            }

            @Override
            public void onPostExecute() {
                if (mDBCallback != null) {
                    mDBCallback.onAction();
                }
            }

        });
        mDBTask.execute();
    }

    private void showDialogEnterName(final IDBCallback mDCallback, String effectName) {
        final EditText mEdName = new EditText(context);
        mEdName.setSingleLine(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("Enter title").setView(mEdName)
                .setPositiveButton("OK", (dialog, which) -> {
                    ApplicationUtils.hiddenVirtualKeyboard(context, mEdName);
                    String mNewName = mEdName.getText().toString().toLowerCase() + "_" + effectName;
                    if (!StringUtils.isEmptyString(mNewName)) {
                        if (StringUtils.isContainsSpecialCharacter(mNewName)) {
                            showToast("Your name can only contain the alphabet or number characters");
                            return;
                        }
                        mNameExportVoice = mNewName + ".wav";
                    }
                    if (mDCallback != null) {
                        mDCallback.onAction();
                    }
                }).setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDCallback != null) {
                            mDCallback.onAction();
                        }
                    }
                });
        AlertDialog mDialogEnterPass = builder.create();
        mDialogEnterPass.show();
    }

    private void onCreateDBMedia() {
        Log.i("vcm", "create db media");
        if (!StringUtils.isEmptyString(mPathAudio)) {
            mDBMedia = new DBMediaPlayer(mPathAudio);
            mDBMedia.prepareAudio();
            mDBMedia.setOnDBMediaListener(new IDBMediaListener() {
                @Override
                public void onMediaError() {
                }

                @Override
                public void onMediaCompletion() {
                    if (playingIndex != null && playingIndex > -1 && playingIndex < effectObjects.size()) {
                        effectObjects.get(playingIndex).setPlaying(false);
                        setPlayingIndex(null);
                        if (playingIndex != null && playingIndex >= 0 && playingIndex < effectObjects.size()) {
                            playEffect(playingIndex, new PlayCallback() {
                                @Override
                                public void onFail() {

                                }
                            });
                        }
                    }
                }
            });
        } else {
            Log.i("vcm", "fail to create db media");
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

//    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
//        reactContext
//                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                .emit(eventName, params);
//    }

    public void onResetState() {
        if (effectObjects != null && effectObjects.size() > 0) {
            for (int i = 0; i < effectObjects.size(); i++) {
                if (effectObjects.get(i).isPlaying()) {
                    effectObjects.get(i).setPlaying(false);
                }
            }
        }
    }

    private File getDir() {
        String dirpath = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + NAME_FOLDER_RECORD;
        File dir = new File(dirpath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
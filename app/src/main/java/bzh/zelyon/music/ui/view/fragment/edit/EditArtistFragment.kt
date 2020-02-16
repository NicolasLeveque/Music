package bzh.zelyon.music.ui.view.fragment.edit

import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Artist
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_edit_artist.*
import kotlinx.android.synthetic.main.view_artwork.view.*
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

class EditArtistFragment private constructor(): AbsEditFragment<Artist>() {

    private var paths = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_edit_artist_inputview_name.text = absModel.name

        absModel.musics.forEach { music ->
            paths.add(music.path)
        }

        apiViewModel.getArtist(absModel.name).observe(absActivity, Observer {
            infosFromLastFM = it?.artist?.bio?.summary.orEmpty() + "\n\n" + it?.artist?.bio?.content.orEmpty()
        })
    }

    override fun getFormLayoutId() = R.layout.fragment_edit_artist

    override fun onClickArtwork() {
        BottomSheetDialog(absActivity).apply {
            setContentView(LayoutInflater.from(absActivity).inflate(R.layout.view_artwork, null, false).apply {
                view_artwork_device.setOnClickListener {
                    getImageOnDevice()
                }
                view_artwork_download.isVisible = false
                view_artwork_delete.setOnClickListener {
                    deleteArtwork()
                }
            })
        }.show()
    }

    override fun onSave() {
        paths.forEach {
            val audioFile = AudioFileIO.read(File(it))
            val tag = audioFile?.tagOrCreateAndSetDefault
            try {
                tag?.setField(FieldKey.ARTIST, fragment_edit_artist_inputview_name.text)
                if (deleteCurrentArtwork) {
                    tag?.deleteArtworkField()
                }
                newArtwork?.let { artwork ->
                    tag?.setField(artwork)
                }
            } catch (e: Exception) {
                absActivity.showSnackbar(getString(R.string.fragment_edit_snackbar_failed))
            } finally {
                audioFile?.commit()
            }
        }
        MediaScannerConnection.scanFile(absActivity, paths.toTypedArray(), null) { _, _ -> back() }
    }

    companion object {

        fun getInstance(artist: Artist, artwork: Bitmap?) = EditArtistFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_ABS_MODEL, artist)
                putParcelable(ARG_ARTORK, artwork)
            }
        }
    }
}
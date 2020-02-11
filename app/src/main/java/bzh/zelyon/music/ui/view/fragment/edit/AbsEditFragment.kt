package bzh.zelyon.music.ui.view.fragment.edit

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import bzh.zelyon.music.R
import bzh.zelyon.music.api.APIViewModel
import bzh.zelyon.music.db.model.AbsModel
import bzh.zelyon.music.ui.component.InputView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarFragment
import bzh.zelyon.music.utils.getLocalFileFromGalleryUri
import bzh.zelyon.music.utils.setImage
import kotlinx.android.synthetic.main.fragment_edit.*
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

abstract class AbsEditFragment<T: AbsModel>: AbsToolBarFragment() {

    lateinit var apiViewModel: APIViewModel
    lateinit var absModel: T
    private var currentArtwork: Drawable? = null
    protected var newArtwork: Artwork? = null
    protected var deleteCurrentArtwork = false
    protected var imageUrlFromLastFM: String? = null
    protected var infosFromLastFM: String? = null
        set(value) {
            field = value
            menu?.findItem(R.id.fragment_edit_info)?.isVisible = !value.isNullOrBlank()
        }
    private val inputviews = mutableListOf<InputView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        absModel = arguments?.getSerializable(ARG_ABS_MODEL) as T
        currentArtwork = (arguments?.getParcelable(ARG_ARTORK) as? Bitmap)?.let {
            BitmapDrawable(absActivity.resources, it)
        } ?: absActivity.getDrawable(absModel.getPlaceholderId())
        apiViewModel = ViewModelProvider(this).get(APIViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment_edit_imageview_artwork.transitionName = absModel.getTransitionName()
        fragment_edit_imageview_artwork.setImage(absModel, currentArtwork)
        LayoutInflater.from(absActivity).inflate(getFormLayoutId(), fragment_edit_layout_form, true)
        getInputViews(view)
    }

    override fun getLayoutId() = R.layout.fragment_edit

    override fun getToolBarTitle() = absModel.getDeclaration()

    override fun getIdToolbar() = R.id.fragment_edit_toolbar

    override fun getIdMenu() = R.menu.fragment_edit

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        when (id) {
            R.id.fragment_edit_info -> AlertDialog.Builder(absActivity).setMessage(infosFromLastFM).show()
            R.id.fragment_edit_save -> if (inputviews.all { it.checkValidity() }) onSave()
            R.id.fragment_edit_imageview_artwork -> onClickArtwork()
        }
    }

    abstract fun getFormLayoutId(): Int

    abstract fun onClickArtwork()

    abstract fun onSave()

    fun getImageOnDevice() {
        absActivity.ifPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            absActivity.startIntentWithResult(Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            }) { _, result ->
                result.data?.let {
                    absActivity.getLocalFileFromGalleryUri(it, absModel.getDeclaration() + ".png")?.let { file ->
                        fragment_edit_imageview_artwork.setImage(File(file.path), absActivity.getDrawable(absModel.getPlaceholderId()))
                        newArtwork = ArtworkFactory.createArtworkFromFile(file)
                        deleteCurrentArtwork = true
                    }
                }
            }
        }
    }

    fun getImageFromLastFM() {
        imageUrlFromLastFM?.let { imageUrlFromLastFM ->
            fragment_edit_imageview_artwork.setImage(imageUrlFromLastFM, absActivity.getDrawable(absModel.getPlaceholderId()))
            newArtwork = ArtworkFactory.createLinkedArtworkFromURL(imageUrlFromLastFM)
            deleteCurrentArtwork = true
        }
    }

    fun deleteArtwork() {
        fragment_edit_imageview_artwork.setImageResource(absModel.getPlaceholderId())
        deleteCurrentArtwork = true
    }

    private fun getInputViews(view: View) {
        if (view is InputView) {
            inputviews.add(view)
        } else if (view is ViewGroup) {
            view.children.forEach { getInputViews(it) }
        }
    }

    companion object {
        const val ARG_ABS_MODEL = "ARG_ABS_MODEL"
        const val ARG_ARTORK = "ARG_ARTORK"
    }
}
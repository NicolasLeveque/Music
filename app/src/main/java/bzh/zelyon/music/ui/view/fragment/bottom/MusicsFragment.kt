package bzh.zelyon.music.ui.view.fragment.bottom

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.Music
import bzh.zelyon.music.ui.component.ItemsView
import bzh.zelyon.music.ui.view.abs.fragment.AbsToolBarBottomSheetFragment
import bzh.zelyon.music.ui.view.fragment.edit.EditMusicFragment
import bzh.zelyon.music.utils.MusicManager
import bzh.zelyon.music.utils.setImage
import kotlinx.android.synthetic.main.fragment_musics.*
import kotlinx.android.synthetic.main.item_music.view.*

class MusicsFragment private constructor(): AbsToolBarBottomSheetFragment() {

    lateinit var musics: List<Music>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musics = arguments?.getSerializable(ARG_MUSICS) as List<Music>
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (fragment_musics_itemsview_musics as ItemsView<Music>).apply {
            idLayoutItem = R.layout.item_music
            helper = MusicHelper()
            items = musics.toMutableList()
        }
    }

    override fun getIdToolbar() = R.id.fragment_musics_toolbar

    override fun getLayoutId() = R.layout.fragment_musics

    override fun getToolBarTitle() = arguments?.getString(ARG_TITLE).toString()

    inner class MusicHelper: ItemsView.Helper<Music>() {
        override fun onBindItem(itemView: View, items: List<Music>, position: Int) {
            val music = items[position]
            itemView.item_music_imageview_artwork.setImage(music, absActivity.getDrawable(R.drawable.ic_music))
            itemView.item_music_imageview_artwork.transitionName = music.getTransitionName()
            itemView.item_music_textview_title.text = music.title
            itemView.item_music_textview_infos.text = music.getInfos(
                title = false,
                artist = false,
                album = true,
                duration = true
            )
            itemView.item_music_imagebutton.setImageResource(R.drawable.ic_more)
            itemView.item_music_imagebutton.setOnClickListener { onItemLongClick(itemView, items, position) }
        }
        override fun onItemClick(itemView: View, items: List<Music>, position: Int) = MusicManager.playMusics(listOf(items[position]))
        override fun onItemLongClick(itemView: View, items: List<Music>, position: Int) {
            val music = items[position]
            val artwork = (itemView.item_music_imageview_artwork.drawable as? BitmapDrawable)?.bitmap
            PopupMenu(absActivity, itemView.item_music_imagebutton).apply {
                menuInflater.inflate(R.menu.item, menu)
                menu.findItem(R.id.item_add).isVisible = MusicManager.isPlayingOrPause
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.item_play -> MusicManager.playMusics(listOf(music))
                        R.id.item_add -> MusicManager.addMusics(listOf(music))
                        R.id.item_edit_infos -> {
                            this@MusicsFragment.dismiss()
                            showFragment(EditMusicFragment.getInstance(music, artwork), transitionView = itemView.item_music_imageview_artwork)
                        }
                        R.id.item_delete -> MusicManager.deleteMusicFile(absActivity, music)
                        R.id.item_playlists -> showFragment(MusicPlaylistsFragment.getInstance(music))
                    }
                    true
                }
            }.show()
        }
    }

    companion object {

        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_MUSICS = "ARG_MUSICS"

        fun getInstance(title: String, musics: List<Music>) = MusicsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putSerializable(ARG_MUSICS, ArrayList(musics))
            }
        }
    }
}
package se.ju.student.robomow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Picasso
import se.ju.student.robomow.R

class CollisionAvoidanceImageFragment: DialogFragment() {
    companion object {
        const val ARG_IMAGE_URL = "image_url"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_collision_avoidance_image, container, false)
        val imageView: ImageView = view.findViewById(R.id.collision_avoidance_image_view)

        val imageUrl = requireArguments().getString(ARG_IMAGE_URL)
        Picasso.get().load(imageUrl).into(imageView)

        val closeButton: Button = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }
}
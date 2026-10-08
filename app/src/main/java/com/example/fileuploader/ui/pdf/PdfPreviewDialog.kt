package com.example.fileuploader.ui.pdf

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.fileuploader.databinding.DialogPdfPreviewBinding

class PdfPreviewDialog : DialogFragment() {
    private var _binding: DialogPdfPreviewBinding? = null
    private val binding
        get() = _binding!!

    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private val pageBitmaps = mutableListOf<Bitmap>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = DialogPdfPreviewBinding.inflate(
            LayoutInflater.from(requireContext())
        )
        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        val uriString = arguments?.getString(ARG_URI)
        if (uriString != null) {
            renderAllPages(Uri.parse(uriString))
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            (resources.displayMetrics.heightPixels * 0.85).toInt()
        )
    }

    private fun renderAllPages(uri: Uri) {
        try {
            parcelFileDescriptor =
                requireContext()
                    .contentResolver
                    .openFileDescriptor(uri, "r")

            if (parcelFileDescriptor == null) {
                binding.textViewPdfTitle.text =
                    "Could not open PDF"
                return
            }
            pdfRenderer = PdfRenderer(
                parcelFileDescriptor!!
            )

            val pageCount = pdfRenderer!!.pageCount
            if (pageCount == 0) {
                binding.textViewPdfTitle.text =
                    "PDF has no pages"
                return
            }
            for (pageIndex in 0 until pageCount) {
                val page = pdfRenderer!!.openPage(pageIndex)
                val bitmap = Bitmap.createBitmap(
                    page.width,
                    page.height,
                    Bitmap.Config.ARGB_8888
                )

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                page.close()
                pageBitmaps.add(bitmap)
            }

            setupViewPager()

        } catch (exception: Exception) {

            binding.textViewPdfTitle.text =
                "PDF preview failed: ${exception.message}"
        }
    }

    private fun setupViewPager() {

        binding.viewPagerPdf.adapter =
            PdfPageAdapter(pageBitmaps)

        updatePageNumber(0)

        binding.viewPagerPdf.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    updatePageNumber(position)
                }
            }
        )
    }

    private fun updatePageNumber(position: Int) {

        val totalPages = pageBitmaps.size

        binding.textViewPageNumber.text =
            "Page ${position + 1} of $totalPages"
    }

    override fun onDestroyView() {

        pageBitmaps.forEach { bitmap ->
            bitmap.recycle()
        }
        pageBitmaps.clear()
        pdfRenderer?.close()
        pdfRenderer = null

        parcelFileDescriptor?.close()
        parcelFileDescriptor = null

        _binding = null
        super.onDestroyView()
    }

    companion object {

        private const val ARG_URI = "pdf_uri"
        fun newInstance(uri: Uri): PdfPreviewDialog {
            return PdfPreviewDialog().apply {
                arguments = Bundle().apply {
                    putString(
                        ARG_URI,
                        uri.toString()
                    )
                }
            }
        }
    }
}
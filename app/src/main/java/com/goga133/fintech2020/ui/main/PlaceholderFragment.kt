package com.goga133.fintech2020.ui.main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.goga133.fintech2020.R
import com.goga133.fintech2020.models.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.data_main.*
import kotlinx.android.synthetic.main.data_main.view.*
import kotlinx.android.synthetic.main.error_main.*
import kotlinx.android.synthetic.main.error_main.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*


class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel

    // Индекс текущей карточки:
    private var cardIndex: Int = 0

    // Индекс текущей страницы:
    private var pageIndex: Int = 1

    // TODO: Переделать через LiveData
    // В силу сжатых сроков, не успеваю сделать, придётся сделать так :(
    private val cardsList: MutableList<Card> = mutableListOf()

    // Максимальное количество карточек
    private var maxCards: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)

        // Реализуем слушателей
        when (arguments?.getInt("section_number")) {
            1 -> observeGetRandomCard(pageViewModel.randomCardsLiveData)
            2 -> observeGetCards(pageViewModel.latestCardsLiveData)
            3 -> observeGetCards(pageViewModel.topCardsLiveData)
            4 -> observeGetCards(pageViewModel.hotCardsLiveData)
        }

        // Логика кнопки "Назад":
        root.retry_button.setOnClickListener {
            loadCards()
        }

        // Логика кнопки "Вперёд":
        root.back_button.setOnClickListener {
            if (cardIndex <= 1)
                root.back_button.isEnabled = false
            cardIndex -= 1
            root.forward_button.isEnabled = true
            setImage()
        }
        root.forward_button.setOnClickListener {
            root.back_button.isEnabled = true

            cardIndex += 1
            if (cardIndex >= cardsList.size) {
                // Если количество картинок превышает максимум:
                if (maxCards == cardIndex + 1) {
                    root.forward_button.isEnabled = false
                    Toast.makeText(this.context, "А дальше листать некуда :(", Toast.LENGTH_LONG)
                        .show()
                }
                // Загружаем новые картинки, т.к старых нет.
                else loadCards()
            }
            // Берём из коллекции старые фотки
            else setImage()
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Загружаем карточки
        loadCards()
    }

    // Загрузка карточек
    private fun loadCards() {
        // Выбираем нужный фрагмент
        when (arguments?.getInt("section_number")) {
            1 -> pageViewModel.getRandomCard()
            2 -> pageViewModel.getCards(Section.LATEST, pageIndex)
            3 -> pageViewModel.getCards(Section.TOP, pageIndex)
            4 -> pageViewModel.getCards(Section.HOT, pageIndex)
        }
    }

    private fun observeGetCards(liveData: LiveData<Event<RequestCard>>) {
        // Назначаем методы для слушателя
        liveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> viewOneLoading()
                Status.SUCCESS -> it.data?.let { data -> viewOneSuccess(data) }
                Status.ERROR -> viewOneError(it.error)
            }
        })
    }

    private fun observeGetRandomCard(liveData: LiveData<Event<Card>>) {
        // Назначаем слушателя для рандомных карточек
        liveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> viewOneLoading()
                Status.SUCCESS -> it.data?.let { data -> viewOneSuccess(data) }
                Status.ERROR -> viewOneError(it.error)
            }
        })
    }

    // Загружаем фотографию и обновляем UI
    private fun setImage(index: Int = cardIndex) {
        view?.let {

            // Если карточка первая, то назад вернуться нельзя:
            if (cardIndex == 0)
                back_button.isEnabled = false

            // Если карточек нет, то загружаем подготовленную фотографию:
            if (cardsList.size == 0) {
                Glide
                    .with(it.context)
                    .load(R.drawable.not_data)
                    .centerCrop()
                    .into(it.findViewById<AppCompatImageView>(R.id.main_image))
                forward_button.isEnabled = false
                loading_progressBar.visibility = View.GONE
            }
            // Успешный вариант загрузки:
            else {
                val requestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(100, 100)

                Glide
                    .with(it)
                    .asGif()
                    .load(
                        cardsList[index].gifURL?.replace(
                            "http://",
                            "https://"
                        )
                    )
                    .placeholder(ColorDrawable(Color.GRAY))
                    .apply(requestOptions)
                    // Для обновления статуса в ProgressBar
                    .listener(object : RequestListener<GifDrawable> {
                        override fun onResourceReady(
                            resource: GifDrawable?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            it.loading_progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            it.loading_progressBar.visibility = View.VISIBLE
                            Snackbar.make(
                                it,
                                "Ошибочка при загрузке гифки.",
                                Snackbar.LENGTH_INDEFINITE
                            )
                            return false
                        }
                    })
                    // Анимации
                    .thumbnail(0.1f)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .centerCrop()
                    .into(it.findViewById<AppCompatImageView>(R.id.main_image))

                if (maxCards ?: -1 > 0)
                    it.counter_textView.text =
                        String.format(getString(R.string.counter), index + 1, maxCards)

                // Анимация для текста:
                it.description_textView.apply {
                    startAnimation(
                        AnimationUtils.loadAnimation(
                            it.context,
                            android.R.anim.fade_in
                        )
                    );

                    // Присваивание текста для описания:
                    text = String.format(
                        getString(R.string.description),
                        cardsList[cardIndex].description
                    )
                }
                // Присваиваем текст для доп.информации:
                it.additional_textView.text = String.format(
                    getString(R.string.additional),
                    cardsList[cardIndex].author,
                    cardsList[cardIndex].date
                )
            }
        }

    }

    // Загрузка данных
    private fun viewOneLoading() {
        view?.let {
            it.loading_progressBar.visibility = View.VISIBLE
            it.forward_button.isEnabled = false
            it.failed_layout.visibility = View.GONE
        }
    }

    // Успешная загрузка данных для одной карточки:
    private fun viewOneSuccess(data: Card?) {
        if (data != null) {
            if (!cardsList.contains(data))
                cardsList.add(data)
            setImage()
        }
        view?.let {
            it.forward_button.isEnabled = true

            it.failed_layout.visibility = View.GONE
            it.success_layout.visibility = View.VISIBLE
        }
    }

    // Успешная загрузка данных для коллекции карточек:
    private fun viewOneSuccess(data: RequestCard?) {
        val elements = data?.result?.toList() ?: listOf()

        if (!cardsList.containsAll(elements)) {
            pageIndex++
            cardsList.addAll(elements)
            maxCards = data?.totalCount
        }
        view?.let {
            it.forward_button.isEnabled = true

            it.failed_layout.visibility = View.GONE
            it.success_layout.visibility = View.VISIBLE
        }
        setImage()
    }

    // При возникновении ошибок при загрузки данных:
    private fun viewOneError(error: Throwable?) {
        if (isVisible) {
            loading_progressBar.visibility = View.INVISIBLE
            failed_layout.visibility = View.VISIBLE
            success_layout.visibility = View.GONE
        }
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        // Присваиваем каждому фрагменту свой номер:
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}
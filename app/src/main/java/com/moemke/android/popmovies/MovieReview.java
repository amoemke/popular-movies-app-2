package com.moemke.android.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aureamoemke on 23/02/2017.
 */
/*
MovieReview (part of MovieDetail json)
https://api.themoviedb.org/3/movie/328111?api_key=YOUR_API_KEY&append_to_response=videos,reviews
    ...
   "reviews": {
        "page": 1,
        "results": [
        {"id": "579cfaac9251411b36008316",
            "author": "Screen Zealots",
            "content": "A SCREEN ZEALOTS REVIEW www.screenzealots.com\r\n\r\nAnyone who is fortunate enough to share their life with a companion animal will undoubtedly get a kick out of the latest Illumination animated effort, “The Secret Life of Pets.” The film soars when it focuses on animals interacting with their human guardians, with the canine and feline characters acting like real pets do (if my cats could talk, I’m sure they’d converse in  similar dialogue as portrayed onscreen). The first part of the movie is incredibly perceptive and clever, as is the last 10 minutes because it zeroes in on these very relationships (the opening and closing scenes of the movie are touching and have lots of heart). The problem comes in the middle when the story stops being about ‘pets being pets.’ Sadly, the majority of the film lags when it ventures into the dreaded animated movie territory of sheer stupidity.\r\n\r\nLoveable human Katie (Ellie Kemper) and her pup Max (Louis C.K.) are the best of friends. Max has several animal buddies that live in the same New York City high rise, including dogs, cats, birds and guinea pigs that stop by for daily visits. When Katie brings home Duke (Eric Stonestreet) from the animal shelter, Max devises a plan to get rid of him. Problem is, the two dogs find themselves lost in the big city and Max’s would-be girlfriend Gidget (Jenny Slate) takes it upon herself to recruit other pets — including the elderly paralyzed basset hound Pops (Dana Carvey) and lonely falcon Tiberius (Albert Brooks) — to bring Max home. Along the way they find themselves at odds with the anarchist gang of “flushed pets,” a group of outspoken, anti-human animals led by former magician’s bunny Snowball (Kevin Hart).\r\n\r\nThe voice acting runs the gamut from phenomenally good (Slate) to wince inducing (Hart). Slate is perfectly cast as Gidget, a poufy white spoiled little dog who eventually saves the day. She proves herself tenfold as a legitimate voiceover actor, and I hope to see her get more work in animation in the future. There’s no denying that Hart is a super likeable actor, but his portrayal of Snowball the bunny is nothing more than repeated, strained yelling. His overall performance felt so labored and unnatural that listening to him onscreen actually made me uncomfortable. I will not hesitate to nominate Hart for a Razzie award for worst actor of the year because his voice work is that bad.\r\n\r\nIn the ‘oh no, not again’ category, there’s plenty of dopey, brainless scenarios crammed in with a feeling that their sole existence is to appease young kids. We get yet another ridiculous animal driving a car stunt that we had to endure in this summer’s nearly insufferable “Finding Dory.” In fact, in “The Secret Life of Pets” we get not only a rabbit driving a van but also a lizard driving a bus and a pig driving a taxi.\r\n\r\nThe absurdity isn’t the only problem: it’s the repetition. The filmmakers must’ve run out of good ideas and instead of moving the story forward, the audience gets the same monotony over and over and over again. I don’t require my animated films to be completely based in reality (there’s a particularly amusing Busby Berkeley inspired musical sequence in a sausage factory), but I do expect more originality than is delivered in this movie. The story at times takes a cynical approach in several places and some of the themes may be too much for sensitive kids (but the film provides a great starting point for a learning opportunity about pets and how animals shouldn’t be viewed as disposable).\r\n\r\nAt least the animation is commendable, nice and colorful with lively, fully realized backgrounds. It’s visually interesting enough for adults and fans of the genre but it’s also vibrant and bustling enough to keep the kids interested. There’s a lovely original score with a lighthearted, almost vintage sound. For me, the original music in this film is one of the standout elements.\r\n\r\nOverall I feel like this film takes a great idea and almost completely wastes the opportunity. This dull, unremarkable action caper is mostly moronic, but the imaginative peek behind the door at an animal’s life when the humans are away is what’s pure gold. I really wish the film had focused on that component. “The Secret Life of Pets” is fine, but isn’t destined for greatness.  \r\n\r\n**A SCREEN ZEALOTS REVIEW www.screenzealots.com**",
            "url": "https://www.themoviedb.org/review/579cfaac9251411b36008316"},
            ...],
        "total_pages": 1,
        "total_results": 3
        }
 */
public class MovieReview implements Parcelable{
    String reviewId;    //"id": "579cfaac9251411b36008316",
    String author;      //"author": "Screen Zealots",
    String content;     //"content": "A SCREEN ZEALOTS REVIEW www.screenzealots.com\r"
    String reviewUrl;   //"url": "https://www.themoviedb.org/review/579cfaac9251411b36008316"

    public MovieReview(String ri, String au, String co, String ru){
        this.reviewId = ri;
        this.author = au;
        this.content = co;
        this.reviewUrl = ru;
        }

    private MovieReview(Parcel in) {
        reviewId = in.readString();
        author = in.readString();
        content = in.readString();
        reviewUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return reviewId + "--" + reviewId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(reviewId);
        parcel.writeString(author);
        parcel.writeString(content);
        parcel.writeString(reviewUrl);
    }

    public static final Parcelable.Creator<MovieReview> CREATOR = new Parcelable.Creator<MovieReview>() {
        @Override
        public MovieReview createFromParcel(Parcel parcel) {
            return new MovieReview(parcel);
        }

        @Override
        public MovieReview[] newArray(int i) {
            return new MovieReview[i];
        }
    };

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }
}

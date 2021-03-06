import {Parameter} from './parameters/parameter';
import {Mechanism} from './mechanism';


export class RatingMechanism extends Mechanism {
    currentRatingValue:number;
    initialRating:number;

    constructor(id: number, type:string, active:boolean, order?:number, canBeActivated?:boolean, parameters?:Parameter[]) {
        super(id, type, active, order, canBeActivated, parameters);
        this.initialRating = this.getParameterValue('defaultRating');
        this.currentRatingValue = this.getParameterValue('defaultRating');
    }

    getRatingElementOptions(): {} {
        var ratingMechanismObject = this;

        return {
            starSize: 25,
            totalStars: this.getParameterValue('maxRating'),
            initialRating: this.initialRating,
            useFullStars: true,
            disableAfterRate: false,
            callback: function (currentRating, $el) {
                ratingMechanismObject.currentRatingValue = currentRating;
            }
        };
    }

    getContext(): any {
        return {
            title: this.getParameterValue('title')
        }
    }
}

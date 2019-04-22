jQuery(function ($) {
	"use strict";
	
	// Activate previously selected or first auth method
	try {
	    if (typeof(Storage) === "undefined") throw 1;

	    var active = sessionStorage.getItem('active-tab', active);
	    if (!active || !/^[a-z]{2,10}-[a-z]{2,10}$/.test(active)) throw 2;

	    if ($('.c-tab-login__nav-link[data-tab="' + active + '"]').length !== 1)
	        throw 3;

	    $('.c-tab-login__nav-link[data-tab="' + active + '"]').addClass('is-active');
	    $('.c-tab-login__content[data-tab="' + active + '"]').addClass('is-active');
	} catch (e) {
	    $('.c-tab-login__nav-link').first().addClass('is-active');
	    $('.c-tab-login__content').first().addClass('is-active');
	}

	// Tab nav
	$(document).on('click', '.c-tab-login__nav-link', function(event){
		event.preventDefault();

		var docwidth = $(document).width();
		var active = $(this).data('tab');

		$('.c-tab-login__nav-item, .c-tab-login__nav-link, .c-tab-login__content').removeClass('is-active');
		$(this).addClass('is-active');
		$('.c-tab-login__content[data-tab="' + active + '"]').addClass('is-active');

		if (typeof(Storage) !== "undefined") {
		    sessionStorage.setItem('active-tab', active);
		}

		$('body').removeClass('is-mobile-subview');
		if (docwidth <= 800 ) {
			$('body').addClass('is-mobile-subview');
			$(this).parents('.c-tab-login__nav-item').addClass('is-active');
		}

	});

	// Mobile back link
	$(document).on('click', '.c-tab-login__nav-back-link', function (event) {
		event.preventDefault();

		$('body').removeClass('is-mobile-subview');
		$('.c-tab-login__nav-item').removeClass('is-active');

	});
	
	// Close alert
	$(document).on('click', '.alert-popup .close', function(event){
		event.preventDefault();
		$(this).closest('.alert').removeClass('show');
	});
	
	
	function validateEstonianIdCode(value){
		return value && /^[0-9]{11}$/.test(value);
	}
	
	function validateEstonianPhoneNumber(value){
		return value && /^[0-9]{3,15}$/.test(value);
	}
	
	function validateFormFieldValue(field, testFunc){
		if (testFunc(field.val())) {
			field.removeClass('is-invalid');
			field.parent('div.input-group').removeClass('is-invalid');
			field.parents('td').children('div.invalid-feedback').addClass('is-hidden');
			return true;
		} else {
			field.addClass('is-invalid');
			field.parent('div.input-group').addClass('is-invalid');
			
			var errorIndex = field.val() ? 1 : 0;
			field.parents('td').children('div.invalid-feedback').each(function(index){
				(index === errorIndex) ? $(this).removeClass('is-hidden') : $(this).addClass('is-hidden');
			});
			
			return false;
		}
	}
	
	// Mobile-ID form submit
	$('#mobileIdForm button.c-btn--primary').on('click', function(event){
		event.preventDefault();
		
		if ($(this).prop('disabled')) return;
		$(this).prop('disabled', true);
		
		var valid = true;
		valid = validateFormFieldValue($('#mid-personal-code'), validateEstonianIdCode) && valid;
		valid = validateFormFieldValue($('#mid-phone-number'), validateEstonianPhoneNumber) && valid;
		
		if (valid) {
			$('#mobileIdForm').submit();
		} else {
			$(this).prop('disabled', false);
		}
	});
	
	// Mobile-ID form submit via input field
	$('#mobileIdForm input.form-control').on('keypress', function(event){
		if (event.keyCode === 13) { // Enter key
			$('#mobileIdForm button.c-btn--primary').trigger('click');
			event.preventDefault();
		}
	});
	
	// Mobile-ID fields validate on focus
	$('#mobileIdForm input.form-control').on('focus', function(){
		validateFormFieldValue($(this), function(){return true;});
	});
});
